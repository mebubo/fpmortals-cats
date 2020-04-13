// Copyright: 2017 - 2018 Sam Halliday, 2020 Zara Turtle
// License: https://firstdonoharm.dev/version/2/1/license.html

package fpmortals
package time

import prelude._

trait LocalClock[F[_]] {
  def now: F[Epoch]
}
object LocalClock extends LocalClockBoilerplate

final class LocalClockTask extends LocalClock[Task] {
  def now: Task[Epoch] = Epoch.now.widenError
}
