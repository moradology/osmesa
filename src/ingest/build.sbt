import Dependencies._

name := "osmesa-ingest"

dependencyOverrides += "com.fasterxml.jackson.core" % "jackson-core" % "2.6.7"
dependencyOverrides += "com.fasterxml.jackson.core" % "jackson-databind" % "2.6.7"
dependencyOverrides += "com.fasterxml.jackson.module" % "jackson-module-scala_2.11" % "2.6.7"

libraryDependencies ++= Seq(
  decline,
  hive,
  //gtGeomesa exclude("com.google.protobuf", "protobuf-java"),
  gtGeotools exclude("com.google.protobuf", "protobuf-java"),
  gtS3 exclude("com.google.protobuf", "protobuf-java"),
  gtSpark exclude("com.google.protobuf", "protobuf-java"),
  gtVector exclude("com.google.protobuf", "protobuf-java"),
  gtVectorTile exclude("com.google.protobuf", "protobuf-java"),
  "com.google.protobuf" % "protobuf-java" % "2.5.0",
  vectorpipe exclude("com.google.protobuf", "protobuf-java"),
  geomesaHbaseDatastore,
  "com.esotericsoftware" % "kryo-shaded" % "4.0.0",
  "org.yaml" % "snakeyaml" % "1.8",
  "org.apache.hadoop" % "hadoop-aws" % "2.8.1",
  "com.amazonaws" % "aws-java-sdk-s3" % "1.10.77",
  // "org.locationtech.geomesa" % "geomesa-security_2.11" % Version.geomesa,
  // "org.locationtech.geomesa" % "geomesa-feature-common_2.11" % Version.geomesa,
  // "org.locationtech.geomesa" % "geomesa-utils_2.11" % Version.geomesa,
  // "org.locationtech.geomesa" % "geomesa-feature-kryo_2.11" % Version.geomesa,
  // "org.locationtech.geomesa" % "geomesa-filter_2.11" % Version.geomesa,
  // "org.locationtech.geomesa" % "geomesa-z3_2.11" % Version.geomesa,
  // "org.locationtech.geomesa" % "geomesa-index-api_2.11" % Version.geomesa,
  cats,
  hbaseClient,
  hbaseCommon,
  hbaseServer,
  scalactic,
  scalatest
)

fork in Test := true

javaOptions ++= Seq("-Xmx5G")

initialCommands in console :=
  """
  """

mainClass in assembly := Some("osmesa.ingest.Main")

assemblyJarName in assembly := "osmesa-ingest.jar"

assemblyShadeRules in assembly := {
  val shadePackage = "com.azavea.shaded.demo"
  Seq(
    ShadeRule.rename("com.google.common.**" -> s"$shadePackage.google.common.@1")
      .inLibrary("com.azavea.geotrellis" %% "geotrellis-cassandra" % Version.geotrellis).inAll,
    ShadeRule.rename("io.netty.**" -> s"$shadePackage.io.netty.@1")
      .inLibrary("com.azavea.geotrellis" %% "geotrellis-hbase" % Version.geotrellis).inAll,
    ShadeRule.rename("com.fasterxml.jackson.**" -> s"$shadePackage.com.fasterxml.jackson.@1")
      .inLibrary("com.networknt" % "json-schema-validator" % "0.1.7").inAll,
    ShadeRule.rename("org.apache.avro.**" -> s"$shadePackage.org.apache.avro.@1")
      .inLibrary("com.azavea.geotrellis" %% "geotrellis-spark" % Version.geotrellis).inAll
  )
}

val meta = """META.INF(.)*""".r
assemblyMergeStrategy in assembly := {
  case s if s.startsWith("META-INF/services") => MergeStrategy.concat
  case PathList("javax", "servlet", xs @ _*) => MergeStrategy.first
  case PathList(ps @ _*) if ps.last endsWith ".html" => MergeStrategy.first
  case n if n.startsWith("reference.conf") => MergeStrategy.concat
  case n if n.endsWith(".conf") => MergeStrategy.concat
  case meta(_) => MergeStrategy.discard
  case _ => MergeStrategy.first
}
