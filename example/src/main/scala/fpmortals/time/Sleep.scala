// Copyright: 2017 - 2018 Sam Halliday, 2020 Zara Turtle
// License: https://firstdonoharm.dev/version/2/1/license.html

package fpmortals
package time

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext

import cats._, implicits._
import cats.effect.IO

trait Sleep[F[_]] { self =>
  def sleep(time: FiniteDuration): F[Unit]

  def mapK[G[_]](f: F ~> G): Sleep[G] = new Sleep[G] {
    def sleep(time: FiniteDuration): G[Unit] = f(self.sleep(time))
  }

}

final class SleepIO extends Sleep[IO] {
  def sleep(time: FiniteDuration): IO[Unit] = IO.sleep(time)(IO.timer(ExecutionContext.global))
}
