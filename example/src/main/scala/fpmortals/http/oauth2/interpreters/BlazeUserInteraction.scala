// Copyright: 2017 - 2018 Sam Halliday, 2020 Zara Turtle
// License: https://firstdonoharm.dev/version/2/1/license.html

package fpmortals
package http
package oauth2
package interpreters

import cats._, implicits._
import cats.effect.IO
import cats.effect.concurrent.Deferred

import scala.concurrent.duration._

import eu.timepit.refined.api.Refined
import eu.timepit.refined.string.Url
import org.http4s._
import org.http4s.dsl._
import org.http4s.server.Server
import org.http4s.server.blaze._

import fpmortals.os.Browser
import fpmortals.time.SleepIO

final class BlazeUserInteraction private (
  pserver: Deferred[IO, Server[IO]],
  ptoken: Deferred[IO, String]
) extends UserInteraction[IO] {
  private val dsl = new Http4sDsl[IO] {}
  private val sleeper = new SleepIO
  import dsl._

  private object Code extends QueryParamDecoderMatcher[String]("code")
  private val service: HttpService[IO] = HttpService[IO] {
    case GET -> Root :? Code(code) =>
      ptoken.complete(code) as
        Ok("That seems to have worked, go back to the console.")
  }

  private val launch: IO[Server[IO]] =
    BlazeBuilder[IO].bindHttp(0, "localhost").mountService(service, "/").start

  def start: IO[String Refined Url] =
    for {
      server  <- launch
      _ <- pserver.complete(server)
    } yield mkUrl(server)

  // the 1 second sleep is necessary to avoid shutting down the server before
  // the response is sent back to the browser (yes, we are THAT quick!)
  def stop: IO[CodeToken] =
    for {
      server <- pserver.get
      token  <- ptoken.get
      _      <- sleeper.sleep(1.second) *> server.shutdown
    } yield CodeToken(token, mkUrl(server))

  def open(url: String Refined Url): IO[Unit] = Browser.open(url)

  private def mkUrl(s: Server[IO]): String Refined Url = {
    val port = s.address.getPort
    Refined.unsafeApply(s"http://localhost:${port.toString}/")
  }

}
object BlazeUserInteraction {
  def apply(): IO[BlazeUserInteraction] = {
    for {
      p1 <- Deferred[IO, Server[IO]]
      p2 <- Deferred[IO, String]
    } yield new BlazeUserInteraction(p1, p2)
  }
}
