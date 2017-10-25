package osmesa.ingest

import org.locationtech.geomesa.utils.geotools.SftBuilder


object OsmFeatureTypes {

  def osmPointFeatureType(sftName: String) = {
    new SftBuilder()
      .longType("fid")
      .stringType("user")
      .longType("userId")
      .longType("changeSet")
      .longType("version")
      .date("created", true)
      .point("geom", true)
      .booleanType("visible")
      .mapType[String, String]("tags")
      .build(sftName)
  }

  def osmLineStringFeatureType(sftName: String) = {
    new SftBuilder()
      .longType("fid")
      .stringType("user")
      .longType("userId")
      .longType("changeSet")
      .longType("version")
      .date("created", true)
      .lineString("geom", true)
      .booleanType("visible")
      .mapType[String, String]("tags")
      .build(sftName)
  }

  def osmMultiPolyFeatureType(sftName: String) = {
    new SftBuilder()
      .longType("fid")
      .stringType("user")
      .longType("userId")
      .longType("changeSet")
      .longType("version")
      .date("created", true)
      .multiPolygon("geom", true)
      .booleanType("visible")
      .mapType[String, String]("tags")
      .build(sftName)
  }
}

