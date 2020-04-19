// Copyright: 2017 - 2018 Sam Halliday, 2020 Zara Turtle
// License: https://firstdonoharm.dev/version/2/1/license.html

package fpmortals
package time

import cats._, implicits._
import cats.effect.IO

trait LocalClock[F[_]] { self =>
  def now: F[Epoch]

  def mapK[G[_]](f: F ~> G): LocalClock[G] = new LocalClock[G] {
    def now: G[Epoch] = f(self.now)
  }
}

final class LocalClockTask extends LocalClock[IO] {
  def now: IO[Epoch] = Epoch.now
}
