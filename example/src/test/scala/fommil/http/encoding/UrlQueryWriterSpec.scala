// Copyright: 2017 - 2018 Sam Halliday, 2020 Zara Turtle
// License: https://firstdonoharm.dev/version/2/1/license.html

package fommil
package http.encoding

import prelude._, Z._

import UrlQueryWriter.ops._

class UrlQueryWriterSpec extends Test {
  "UrlQueryWriter"
    .should("encode case classes")
    .in(
      Foo("http://foo", 10, "%").toUrlQuery.params.shouldBe(
        IList(
          "apple"   -> "http://foo",
          "bananas" -> "10",
          "pears"   -> "%"
        )
      )
    )
}
