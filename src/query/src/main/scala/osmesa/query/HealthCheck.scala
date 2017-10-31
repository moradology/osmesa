package osmesa.query

import io.circe._
import io.circe.generic.JsonCodec


sealed trait HealthCheckStatus

object HealthCheckStatus {
  case object OK extends HealthCheckStatus { override def toString = "ok" }
  case object Failing extends HealthCheckStatus { override def toString = "failing" }

  def fromString(str: String): Option[HealthCheckStatus] = str.toLowerCase match {
    case "ok" => Some(OK)
    case "failing" => Some(Failing)
    case _ => None
  }

  implicit val healthcheckEncoder: Encoder[HealthCheckStatus] =
    Encoder.encodeString.contramap[HealthCheckStatus](_.toString)

  implicit val healthcheckDecoder: Decoder[HealthCheckStatus] =
    Decoder[String].emap({ str =>
      HealthCheckStatus.fromString(str) match {
        case Some(status) => Right(status)
        case None => Left(s"Unable to parse string (${str}) as healthcheck status")
      }
    })
}

/**
  * Overall healthcheck for Raster Foundry
  *
  * @param status   status of raster foundry (e.g. OK, UNHEALTHY)
  * @param services list of individual service checks
  *
  */
@JsonCodec
case class HealthCheck(status: HealthCheckStatus, services: Seq[ServiceCheck])

/**
  * Individual service check for a component
  *
  * @param service name of service that check is for
  * @param status  status of service (e.g. OK, UNHEALTHY)
  */
@JsonCodec
case class ServiceCheck(service: String, status: HealthCheckStatus)

/**
  * Exception for database errors
  *
  * @param description description of error
  */
object HealthCheckService {

  /**
    * Perform healthcheck by verifying at least the following:
    *   - datastore is accessible
    *
    */
  def healthCheck = {
    // TODO: actually check datastore here!!!
    Option(1) match {
      case Some(count) =>
        HealthCheck(
          HealthCheckStatus.OK,
          Seq(ServiceCheck("datastore", HealthCheckStatus.OK))
        )
      case _ =>
        HealthCheck(
          HealthCheckStatus.Failing,
          Seq(ServiceCheck("datastore", HealthCheckStatus.Failing))
        )
    }
  }
}

