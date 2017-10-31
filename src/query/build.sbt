import Dependencies._

name := "osmesa-query"

libraryDependencies ++= Seq(
  decline,
  gmHBaseStore,
  cats,
  akka,
  akkaStream,
  akkaHttp,
  akkaTestkit,
  circeCore,
  circeGeneric,
  circeExtras,
  circeParser,
  circeOptics,
  akkaCirceJson,
  hbaseClient,
  hbaseCommon,
  tsConfig,
  akkaHttpExt,
  "org.apache.hadoop" % "hadoop-common" % "2.7.3"

)

fork in Test := true

javaOptions ++= Seq("-Xmx5G")

initialCommands in console :=
  """
  """

assemblyJarName in assembly := "osmesa-query.jar"

assemblyShadeRules in assembly := {
  val shadePackage = "com.azavea.shaded.demo"
  Seq(
    ShadeRule.rename("com.google.common.**" -> s"$shadePackage.google.common.@1")
      .inLibrary("com.azavea.geotrellis" %% "geotrellis-cassandra" % Version.geotrellis).inAll,
    ShadeRule.rename("io.netty.**" -> s"$shadePackage.io.netty.@1")
      .inLibrary("com.azavea.geotrellis" %% "geotrellis-hbase" % Version.geotrellis).inAll,
    ShadeRule.rename("com.fasterxml.jackson.**" -> s"$shadePackage.com.fasterxml.jackson.@1")
      .inLibrary("com.networknt" % "json-schema-validator" % "0.1.7").inAll
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
