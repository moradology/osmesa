package osmesa.query

import org.locationtech.geomesa.hbase.data._
import org.geotools.filter.text.ecql.ECQL
import org.opengis.feature.Feature
import org.geotools.data._
import io.circe.generic.JsonCodec


@JsonCodec
case class OSMQuery(ecql: String, typeName: String, page: Int)

object OSMQuery {

  val pageSize = 20

  def paginatedResponse(ds: HBaseDataStore, ecql: String, typeName: String, page: Int) = {
    val featureSource = ds.getFeatureSource(typeName)
    val filter = ECQL.toFilter(ecql)

    val query = new org.geotools.data.Query(typeName, filter)
    query.setStartIndex(pageSize * (page - 1))
    query.setMaxFeatures(pageSize)

    var features = scala.collection.mutable.MutableList[Feature]()
    val iter = featureSource.getFeatures(query).features
    try {
      while (iter.hasNext) {
        features += iter.next
      }
    } finally {
      iter.close()
    }
  }
}
