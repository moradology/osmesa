import sbt._

object Dependencies {
  val decline       = "com.monovore"                %% "decline"                     % Version.decline
  val hive          = "org.apache.spark"            %% "spark-hive"                  % Version.hive % "provided"
  val gtGeomesa     = "org.locationtech.geotrellis" %% "geotrellis-geomesa"          % Version.geotrellis
  val gtGeotools    = "org.locationtech.geotrellis" %% "geotrellis-geotools"         % Version.geotrellis
  val gtS3          = "org.locationtech.geotrellis" %% "geotrellis-s3"               % Version.geotrellis
  val gtSpark       = "org.locationtech.geotrellis" %% "geotrellis-spark"            % Version.geotrellis
  val gtVector      = "org.locationtech.geotrellis" %% "geotrellis-vector"           % Version.geotrellis
  val gtVectorTile  = "org.locationtech.geotrellis" %% "geotrellis-vectortile"       % Version.geotrellis
  val vectorpipe    = "com.azavea"                  %% "vectorpipe"                  % Version.vectorpipe
  val cats          = "org.typelevel"               %% "cats"                        % Version.cats
  val scalactic     = "org.scalactic"               %% "scalactic"                   % Version.scalactic
  val scalatest     = "org.scalatest"               %%  "scalatest"                  % Version.scalatest % "test"
  val jaiCore       = "javax.media" % "jai_core"    % "1.1.3"                        % "test" from "http://download.osgeo.org/webdav/geotools/javax/media/jai_core/1.1.3/jai_core-1.1.3.jar"
  val hbaseCommon   = "org.apache.hbase"            % "hbase-common"                 % Version.hbase
  val hbaseClient   = "org.apache.hbase"            % "hbase-client"                 % Version.hbase
  val hbaseServer   = "org.apache.hbase"            % "hbase-server"                 % Version.hbase
  val gmHBaseStore  = "org.locationtech.geomesa"    % "geomesa-hbase-datastore_2.11" % Version.geomesa
  val kryo          = "com.esotericsoftware"        % "kryo-shaded"                  % Version.kryo
  val snakeyaml     = "org.yaml"                    % "snakeyaml"                    % Version.snakeyaml
  val protobuf      = "com.google.protobuf"         % "protobuf-java"                % Version.protobuf
  val akka          = "com.typesafe.akka"           %% "akka-actor"                  % Version.akka
  val akkaStream    = "com.typesafe.akka"           %% "akka-stream"                 % Version.akka
  val akkaHttp      = "com.typesafe.akka"           %% "akka-http"                   % Version.akkaHttp
  val akkaTestkit   = "com.typesafe.akka"           %% "akka-http-testkit"           % Version.akkaHttp
  val akkaSlf4j     = "com.typesafe.akka"           %% "akka-slf4j"                  % Version.akkaSlf4j
  val scalaLogging  = "com.typesafe.scala-logging"  %% "scala-logging"               % Version.scalaLogging
  val logback       = "ch.qos.logback"              %  "logback-classic"             % Version.logbackClassic
  val circeCore     = "io.circe"                    %% "circe-core"                  % Version.circe
  val circeGeneric  = "io.circe"                    %% "circe-generic"               % Version.circe
  val circeExtras   = "io.circe"                    %% "circe-generic-extras"        % Version.circe
  val circeParser   = "io.circe"                    %% "circe-parser"                % Version.circe
  val circeOptics   = "io.circe"                    %% "circe-optics"                % Version.circe
  val akkaCirceJson = "de.heikoseeberger"           %% "akka-http-circe"             % Version.akkaCirceJson
  val tsConfig      = "com.typesafe"                %  "config"                      % Version.tsConfig
  //val kamon         = "io.kamon"                    %% "kamon-core"                  % Version.kamon
  //val kamonAkka     = "io.kamon"                    %% "kamon-akka"                  % Version.kamon
  //val kamonStatsd   = "io.kamon"                    %% "kamon-statsd"                % Version.kamon
  //val kamonAkkaHttp = "io.kamon"                    %% "kamon-akka-http"             % Version.kamonAkkaHttp
  val akkaHttpExt   = "com.lonelyplanet"            %% "akka-http-extensions"        % Version.akkaHttpExtensions
}

