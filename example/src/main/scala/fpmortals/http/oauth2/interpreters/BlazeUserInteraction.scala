// Copyright: 2017 - 2018 Sam Halliday, 2020 Zara Turtle
// License: https://firstdonoharm.dev/version/2/1/license.html

package fpmortals
package http
package oauth2
package interpreters

import cats._, implicits._
import cats.effect.{ ConcurrentEffect, Effect, IO }
import cats.effect.concurrent.Deferred

import scala.concurrent.duration._

import eu.timepit.refined.api.Refined
import eu.timepit.refined.string.Url
import org.http4s._
import org.http4s.dsl._
import org.http4s.server.Server
import org.http4s.server.blaze._

import fpmortals.os.Browser
import fpmortals.time.Sleep

final class BlazeUserInteraction[F[_]: Effect] private (
  S: Sleep[F],
  pserver: Deferred[F, Server[F]],
  ptoken: Deferred[F, String]
) extends UserInteraction[F] {
  private val dsl = new Http4sDsl[F] {}
  import dsl._

  private object Code extends QueryParamDecoderMatcher[String]("code")
  private val service: HttpService[F] = HttpService[F] {
    case GET -> Root :? Code(code) =>
      ptoken.complete(code) >>
        Ok("That seems to have worked, go back to the console.")
  }

  private val launch: F[Server[F]] =
    BlazeBuilder[F].bindHttp(0, "localhost").mountService(service, "/").start

  def start: F[String Refined Url] =
    for {
      server  <- launch
      _ <- pserver.complete(server)
    } yield mkUrl(server)

  // the 1 second sleep is necessary to avoid shutting down the server before
  // the response is sent back to the browser (yes, we are THAT quick!)
  def stop: F[CodeToken] =
    for {
      server <- pserver.get
      token  <- ptoken.get
      _      <- S.sleep(1.second) *> server.shutdown
    } yield CodeToken(token, mkUrl(server))

  def open(url: String Refined Url): F[Unit] = Effect[F].liftIO(Browser.open(url))

  private def mkUrl(s: Server[F]): String Refined Url = {
    val port = s.address.getPort
    Refined.unsafeApply(s"http://localhost:${port.toString}/")
  }

}
object BlazeUserInteraction {
  def apply[F[_]: ConcurrentEffect](S: Sleep[F]): F[BlazeUserInteraction[F]] = {
    for {
      p1 <- Deferred[F, Server[F]]
      p2 <- Deferred[F, String]
    } yield new BlazeUserInteraction(S, p1, p2)
  }
}
