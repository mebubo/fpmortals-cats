// Copyright: 2017 - 2018 Sam Halliday, 2020 Zara Turtle
// License: https://firstdonoharm.dev/version/2/1/license.html

package fpmortals
package http

import cats._, implicits._

import jsonformat.JsDecoder
import eu.timepit.refined.api.Refined
import eu.timepit.refined.string.Url

import scala.util.control.NoStackTrace

import http.encoding._

/**
 * An algebra for issuing basic GET / POST requests to a web server that returns
 * JSON. Errors are captured in the `F[_]`.
 */
trait JsonClient[F[_]] { self =>

  def get[A: JsDecoder](
    uri: String Refined Url,
    headers: List[(String, String)]
  ): F[A]

  // using application/x-www-form-urlencoded
  def post[P: UrlEncodedWriter, A: JsDecoder](
    uri: String Refined Url,
    payload: P,
    headers: List[(String, String)]
  ): F[A]

  def mapK[G[_]](f: F ~> G): JsonClient[G] = new JsonClient[G] {
    def get[A: JsDecoder](
      uri: String Refined Url,
      headers: List[(String, String)]
    ): G[A] = f(self.get(uri, headers))

    def post[P: UrlEncodedWriter, A: JsDecoder](
      uri: String Refined Url,
      payload: P,
      headers: List[(String, String)]
    ): G[A] = f(self.post(uri, payload, headers))
  }
}
object JsonClient {
  sealed abstract class Error extends Throwable with NoStackTrace
  final case class ServerError(status: Int)       extends Error
  final case class DecodingError(message: String) extends Error
}
