package osmesa.common.streaming

import java.io.File
import java.net.URI
import java.nio.charset.StandardCharsets

import cats.implicits._
import com.amazonaws.services.s3.model.AmazonS3Exception
import com.softwaremill.macmemo.memoize
import geotrellis.spark.io.s3.{AmazonS3Client, S3Client}
import geotrellis.vector.io._
import geotrellis.vector.io.json.JsonFeatureCollectionMap
import io.circe.generic.auto._
import io.circe.{yaml, _}
import org.apache.commons.io.IOUtils
import org.apache.spark.internal.Logging
import org.joda.time.DateTime
import osmesa.common.model.AugmentedDiffFeature

import scala.concurrent.duration.{Duration, _}

object AugmentedDiffSource extends Logging {
  val Delay: Duration = 15.seconds

  private lazy val s3: AmazonS3Client = S3Client.DEFAULT

  private implicit val dateTimeDecoder: Decoder[DateTime] =
    Decoder.instance(a => a.as[String].map(DateTime.parse))

  def getSequence(
    baseURI: URI,
    sequence: Int
  ): Seq[(Option[AugmentedDiffFeature], AugmentedDiffFeature)] = {
    val bucket = baseURI.getHost
    val prefix = new File(baseURI.getPath.drop(1)).toPath
    val key = prefix.resolve(s"$sequence.json").toString

    logDebug(s"Fetching sequence $sequence")
    try {
      IOUtils
        .toString(s3.readBytes(bucket, key), StandardCharsets.UTF_8.toString)
        .lines
        .map { line =>
          // Spark doesn't like RS-delimited JSON; perhaps Spray doesn't either
          val features = line
            .replace("\u001e", "")
            .parseGeoJson[JsonFeatureCollectionMap]
            .getAll[AugmentedDiffFeature]

          (features.get("old"), features("new"))
        }
        .toSeq
    } catch {
      case e: AmazonS3Exception if e.getStatusCode == 404 =>
        // sequence is missing; this is intentional, so compare with currentSequence for validity
        if (getCurrentSequence(baseURI) > sequence) {
          Seq.empty[(Option[AugmentedDiffFeature], AugmentedDiffFeature)]
        } else {
          logDebug(s"$sequence is not yet available, sleeping.")
          Thread.sleep(Delay.toMillis)
          getSequence(baseURI, sequence)
        }
      case _: Throwable =>
        logDebug(s"$sequence was unavailable, sleeping before retrying.")
        Thread.sleep(Delay.toMillis)
        getSequence(baseURI, sequence)
    }
  }

  private[streaming] def createOffsetForCurrentSequence(
    baseURI: URI
  ): SequenceOffset =
    SequenceOffset(getCurrentSequence(baseURI))

  @memoize(maxSize = 1, expiresAfter = 30 seconds)
  def getCurrentSequence(baseURI: URI): Int = {
    val bucket = baseURI.getHost
    val prefix = new File(baseURI.getPath.drop(1)).toPath
    val key = prefix.resolve("state.yaml").toString

    val body = IOUtils
      .toString(s3.readBytes(bucket, key), StandardCharsets.UTF_8.toString)

    val state = yaml.parser
      .parse(body)
      .leftMap(err => err: Error)
      .flatMap(_.as[AugmentedDiffState])
      .valueOr(throw _)

    logDebug(s"$baseURI state: ${state.sequence} @ ${state.last_run}")

    state.sequence
  }

  case class AugmentedDiffState(last_run: DateTime, sequence: Int)
}