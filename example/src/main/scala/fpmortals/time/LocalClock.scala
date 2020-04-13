// Copyright: 2017 - 2018 Sam Halliday, 2020 Zara Turtle
// License: https://firstdonoharm.dev/version/2/1/license.html

package fpmortals
package time

import cats._, implicits._
import cats.effect.IO

trait LocalClock[F[_]] {
  def now: F[Epoch]
}

final class LocalClockTask extends LocalClock[IO] {
  def now: IO[Epoch] = Epoch.now
}
