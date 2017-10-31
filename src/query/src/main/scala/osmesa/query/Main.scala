package osmesa.query

import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model._
import akka.http.scaladsl.Http
import cats.implicits._
import com.lonelyplanet.akka.http.extensions.PaginationDirectives
import de.heikoseeberger.akkahttpcirce.ErrorAccumulatingCirceSupport._
import com.monovore.decline._
import org.locationtech.geomesa.hbase.data.HBaseDataStore
import org.geotools.data._

import scala.collection.JavaConverters._
import scala.util.Try


object Main extends CommandApp(
  name = "Launch a server for parsing ECQL and returning paginated results",
  header = "Launch a server for parsing ECQL and returning paginated results",
  main = {
    val hostOpt = Opts.option[String]("host", short = "h", help = "The hostname for this process").withDefault("0.0.0.0")
    val portOpt = Opts.option[Int]("port", short = "p", help = "The port exposed to communicate with this server").withDefault(9000)
    val catalogOpt = Opts.option[String]("catalog", short = "c", help = "An HBase catalog for this server to query.")

    (hostOpt, portOpt, catalogOpt).mapN({ (host, port, catalog) =>
      implicit val system = AkkaSystem.system
      implicit val materializer = AkkaSystem.materializer

      sys.addShutdownHook {
        Try(system.terminate())
      }

      val datastore = DataStoreFinder
        .getDataStore(Map("hbase.catalog" -> catalog).asJava)
        .asInstanceOf[HBaseDataStore]

      val routes =
        pathPrefix("healthcheck") {
          pathEndOrSingleSlash {
            get {
              complete {
                HealthCheckService.healthCheck
              }
            }
          }
        } ~
        pathPrefix("query") {
          post {
            entity(as[OSMQuery]) { osmQuery =>
              complete {
                OSMQuery.paginatedResponse(datastore, osmQuery.ecql, osmQuery.typeName, osmQuery.page)
              }
            }
          }
        }

      Http().bindAndHandle(routes, host, port)
    })
  }
)

