// Copyright: 2017 - 2018 Sam Halliday, 2020 Zara Turtle
// License: https://firstdonoharm.dev/version/2/1/license.html

package fommil
package time

import prelude._, S._, Z._

final class EpochSpec extends Test {

  val time1: Epoch = epoch"2017-03-03T18:07:00Z"
  val time2: Epoch = epoch"2017-03-03T18:59:00Z"

  "Epoch".should("calculate time differences").in {
    (time1 + 52.minutes).shouldBe(time2)

    (time2 - time1).shouldBe(52.minutes)

    (time2 - 52.minutes).shouldBe(time1)
  }
}
