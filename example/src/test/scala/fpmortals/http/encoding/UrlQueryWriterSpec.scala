// Copyright: 2017 - 2018 Sam Halliday, 2020 Zara Turtle
// License: https://firstdonoharm.dev/version/2/1/license.html

package fpmortals
package http.encoding

import cats._, implicits._

import UrlQueryWriter.ops._

import fpmortals.prelude.Test

class UrlQueryWriterSpec extends Test {
  "UrlQueryWriter"
    .should("encode case classes")
    .in(
      Foo("http://foo", 10, "%").toUrlQuery.params.shouldBe(
        List(
          "apple"   -> "http://foo",
          "bananas" -> "10",
          "pears"   -> "%"
        )
      )
    )
}
