// Copyright: 2017 - 2018 Sam Halliday, 2020 Zara Turtle
// License: https://firstdonoharm.dev/version/2/1/license.html

package fommil
package time

import prelude._

trait Sleep[F[_]] {
  def sleep(time: FiniteDuration): F[Unit]
}
object Sleep extends SleepBoilerplate

final class SleepTask extends Sleep[Task] {
  def sleep(time: FiniteDuration): Task[Unit] = Task.sleep(time)
}
