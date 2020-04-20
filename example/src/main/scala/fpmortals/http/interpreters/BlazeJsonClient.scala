// Copyright: 2017 - 2018 Sam Halliday, 2020 Zara Turtle
// License: https://firstdonoharm.dev/version/2/1/license.html

package fpmortals
package http
package interpreters

import cats._, implicits._
import cats.effect.{ Effect, IO }

import java.lang.Throwable
import scala.collection.immutable.List

import jsonformat._
import jsonformat.JsDecoder.ops._

import eu.timepit.refined.api.Refined
import eu.timepit.refined.string.Url

import http.encoding._
import UrlEncodedWriter.ops._

import org.http4s
import org.http4s.{ EntityEncoder, MediaType }
import org.http4s.headers.`Content-Type`
import org.http4s.client.Client
import org.http4s.client.blaze.{ BlazeClientConfig, Http1Client }

final class BlazeJsonClient[F[_]] private (H: Client[F])(
  implicit F: Effect[F]
) extends JsonClient[F] {

  def get[A: JsDecoder](
    uri: String Refined Url,
    headers: List[(String, String)]
  ): F[A] =
      H.fetch(
        http4s.Request[F](
          uri = convert(uri),
          headers = convert(headers)
        )
      )(handler[A]).flatMap(F.fromEither)

  private implicit val encoder: EntityEncoder[F, String Refined UrlEncoded] =
    EntityEncoder[F, String]
      .contramap[String Refined UrlEncoded](_.value)
      .withContentType(
        `Content-Type`(MediaType.`application/x-www-form-urlencoded`)
      )

  def post[P: UrlEncodedWriter, A: JsDecoder](
    uri: String Refined Url,
    payload: P,
    headers: List[(String, String)]
  ): F[A] =
      H.fetch(
        http4s
          .Request[F](
            method = http4s.Method.POST,
            uri = convert(uri),
            headers = convert(headers)
          )
          .withBody(
            payload.toUrlEncoded
          )
      )(handler[A]).flatMap(F.fromEither)

  private[this] def convert(headers: List[(String, String)]): http4s.Headers =
    http4s.Headers(
      headers.foldRight(List[http4s.Header]()) {
        case ((key, value), acc) => http4s.Header(key, value) :: acc
      }
    )

  // we already validated the Url. If this fails, it's a bug in http4s
  private[this] def convert(uri: String Refined Url): http4s.Uri =
    http4s.Uri.unsafeFromString(uri.value)

  private[this] def handler[A: JsDecoder](
    resp: http4s.Response[F]
  ): F[Either[JsonClient.Error, A]] =
    if (!resp.status.isSuccess)
      F.pure(Left(JsonClient.ServerError(resp.status.code)))
    else
      for {
        text <- resp.body.through(fs2.text.utf8Decode).compile.foldMonoid
        res = JsParser(text)
          .flatMap(_.as[A])
          .leftMap(JsonClient.DecodingError(_))
      } yield res

}
object BlazeJsonClient {
  def apply[F[_]: Effect]: F[JsonClient[F]] =
    Http1Client(BlazeClientConfig.defaultConfig).map(new BlazeJsonClient(_))
}
