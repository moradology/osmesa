package osmesa

import geotrellis.proj4.{LatLng, WebMercator, Transform}
import geotrellis.spark._
import geotrellis.spark.io._
import geotrellis.spark.io.file._
import geotrellis.spark.io.index.ZCurveKeyIndexMethod
import geotrellis.spark.io.s3.{S3AttributeStore, S3LayerWriter}
import geotrellis.spark.tiling._
import geotrellis.vector._
import geotrellis.vector.io._
import geotrellis.vectortile.VectorTile
import org.apache.log4j.{Level, Logger}
import org.apache.spark._
import org.apache.spark.rdd.RDD
import org.apache.spark.sql._
import vectorpipe._
import vectorpipe.util.LayerMetadata
import cats.implicits._
import com.monovore.decline._

import spray.json._

import org.geotools.data.DataStore

object Util {
  import spray.json._
  import spray.json.DefaultJsonProtocol._
  import vectorpipe.osm._
  import vectorpipe.util._
  import java.io.ByteArrayInputStream
  import geotrellis.spark.io.s3.S3Client
  import com.amazonaws.services.s3.model.{ObjectMetadata, AmazonS3Exception}

  implicit object ElementMetaWriter  extends JsonWriter[ElementMeta] {
    def write(m: ElementMeta): JsValue =
      JsObject(
        "id" -> JsNumber(m.id),
        "user" -> JsString(m.user),
        "userId" -> JsString(m.userId),
        "changeSet" -> JsNumber(m.changeSet),
        "version" -> JsNumber(m.version),
        "timestamp" -> JsString(m.timestamp.toString),
        "visible" -> JsBoolean(m.visible)
      )
  }

  implicit object ElementDataWriter  extends JsonWriter[ElementData] {
    def write(d: ElementData): JsValue =
      JsObject(
        "meta" -> d.meta.toJson,
        "tags" -> d.tagMap.toJson
      )
  }
  val s3Client: S3Client = S3Client.DEFAULT

  def logClipFail(e: Extent, f: osm.OSMFeature): Unit = {
    val txt = f.toGeoJson
    val fid = f.data.meta.id
    val gType =
      f.geom match {
        case gc: GeometryCollection => "gc"
        case mp: MultiPolygon => "mp"
        case p: Polygon => "p"
        case ml: MultiLine => "ml"
        case l: Line => "l"
        case mp: MultiPoint => "mp"
        case p: Point => "p"
      }

    val name = s"finland-run1/${gType}-${fid}.json"
    val is = new ByteArrayInputStream(txt.getBytes("UTF-8"))
    s3Client.putObject("vectortiles", s"rde/clip-log/${name}", is, new ObjectMetadata())
  }
}

// object Ingest {
//   def getDataStore(table: String):
// }

object IngestApp extends CommandApp(
  name = "osmesa-ingest",
  header = "Ingest OSM ORC into GeoMesa instance",
  main = {

    /* CLI option handling */
    val orcO = Opts.option[String]("orc", help = "Location of the .orc file to process")
    val bucketO = Opts.option[String]("bucket", help = "S3 bucket to write VTs to")
    val prefixO = Opts.option[String]("key", help = "S3 directory (in bucket) to write to")
    val layerO = Opts.option[String]("layer", help = "Name of the output Layer")
    val localF = Opts.flag("local", help = "Is this to be run locally, not on EMR?").orFalse

    (orcO |@| bucketO |@| prefixO |@| layerO |@| localF).map { (orc, bucket, prefix, layer, local) =>

      println(s"ORC: ${orc}")
      println(s"OUTPUT: ${bucket}/${prefix}")
      println(s"LAYER: ${layer}")

      /* Settings compatible for both local and EMR execution */
      val conf = new SparkConf()
        .setIfMissing("spark.master", "local[*]")
        .setAppName("vp-orc-io")
        .set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
        .set("spark.kryo.registrator", classOf[geotrellis.spark.io.kryo.KryoRegistrator].getName)

      implicit val ss: SparkSession = SparkSession.builder
        .config(conf)
        .enableHiveSupport
        .getOrCreate

      /* Silence the damn INFO logger */
      Logger.getRootLogger().setLevel(Level.ERROR)

      /* For writing a compressed Tile Layer */
      val writer = S3LayerWriter(S3AttributeStore(bucket, prefix))
      // val writer = FileLayerWriter(FileAttributeStore("/home/colin/tiles/"))

      val layout: LayoutDefinition =
        ZoomedLayoutScheme.layoutForZoom(14, WebMercator.worldExtent, 512)

      val df = ss.read.orc(orc)
      //      val df = ss.read.orc("s3://osm-pds/planet/planet-latest.orc")

      // val southAmericaBeforeJune =
      //   df.where("timestamp <= '2017-06-01 00:00:00.0' AND (type != 'node' OR (lon <= -34.767608642578125 AND lon >= -81.33865356445312 AND lat <= 12.623252653219012 AND lat >= -55.98609153380838))")

      // val southAmerica =
      //   df.where("type != 'node' OR (lon <= -34.767608642578125 AND lon >= -81.33865356445312 AND lat <= 12.623252653219012 AND lat >= -55.98609153380838)")

      val targetDf = df//.repartition(1000)

      // Test log clip fail
      val ff = Feature(Point(1,1), osm.ElementData(osm.ElementMeta(1L, "Asdf", "asdf", 2L, 3L, 32423423L, true), Map()))

      Util.logClipFail(Extent(0, 0, 1, 1), ff)

      val (ns, ws, rs) = osm.fromDataFrame(targetDf)

      val numPartitions = 10000
      val nodePartitioner = new HashPartitioner(numPartitions)
      val wayPartitioner = new HashPartitioner(numPartitions)

      /* Reproject nodes */
      val reprojectedNodes =
        ns.partitionBy(nodePartitioner).mapPartitions({ partition =>
          val transform = Transform(LatLng, WebMercator)
          partition.map { case (nodeId, node) =>
            val (lon, lat) = transform(node.lon, node.lat)
            (nodeId, node.copy(lon = lon, lat = lat))
          }
        }, preservesPartitioning = true)

      /* Assumes that OSM ORC is in LatLng */
      val feats: RDD[osm.OSMFeature] =
        // osm.toFeatures(VectorPipe.logToLog4j, reprojectedNodes, ws.partitionBy(wayPartitioner), rs)
        osm.toFeatures(VectorPipe.logToLog4j, reprojectedNodes.map(_._2), ws.partitionBy(wayPartitioner).map(_._2), rs.map(_._2))

      /* Associated each Feature with a SpatialKey */
      val fgrid: RDD[(SpatialKey, Iterable[osm.OSMFeature])] =
        //        VectorPipe.toGrid(Clip.byHybrid, Util.logClipFail, layout, feats, new HashPartitioner(numPartitions))
        //        VectorPipe.toGrid(Clip.byHybrid, Util.logClipFail, layout, feats)
        VectorPipe.toGrid(Clip.byHybrid, VectorPipe.logToLog4j, layout, feats)

      /* Create the VectorTiles */
      val tiles: RDD[(SpatialKey, VectorTile)] =
        VectorPipe.toVectorTile(Collate.byOSM, layout, fgrid).cache()

      val bounds: KeyBounds[SpatialKey] =
        tiles.map({ case (key, _) => KeyBounds(key, key) }).reduce(_ combine _)

      /* Construct metadata for the Layer */
      val meta = LayerMetadata(layout, bounds)

      /* Write the tiles */
      writer.write(LayerId(layer, 14), ContextRDD(tiles, meta), ZCurveKeyIndexMethod)

      ss.stop()

      println("Done.")
    }
  }
)
