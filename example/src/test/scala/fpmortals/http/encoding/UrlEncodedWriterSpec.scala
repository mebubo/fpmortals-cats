// Copyright: 2017 - 2018 Sam Halliday, 2020 Zara Turtle
// License: https://firstdonoharm.dev/version/2/1/license.html

package fpmortals
package http.encoding

import cats._, data._, implicits._

import scala.Left
import eu.timepit.refined.api._
import eu.timepit.refined.refineV
import eu.timepit.refined.auto._
import eu.timepit.refined.string.Url

import UrlEncodedWriter.ops._

import fpmortals.prelude.Test

final case class Foo(apple: String, bananas: Long, pears: String)
object Foo {
  implicit val urlEncodedWriter: UrlEncodedWriter[Foo] = UrlEncodedWriterMagnolia.gen
  implicit val urlQueryWriter: UrlQueryWriter[Foo] = DerivedUrlQueryWriter.gen
}

class UrlEncodedWriterSpec extends Test {
  "UrlEncodedWriter".should("encode Strings").in {
    "foo".toUrlEncoded.shouldBe("foo": String Refined UrlEncoded)

    "http://foo".toUrlEncoded.shouldBe(
      "http%3A%2F%2Ffoo": String Refined UrlEncoded
    )
  }

  it.should("not validate partially encoded strings").in {
    refineV[UrlEncoded]("http%3A%2F foo").shouldBe(
      Left("Predicate failed: http%3A%2F foo.")
    )
  }

  it.should("encode Long numbers").in {
    10L.toUrlEncoded.shouldBe("10": String Refined UrlEncoded)
  }

  it.should("encode stringy maps").in {
    val stringy = List(
      "apple"   -> "http://foo",
      "bananas" -> "10",
      "pears"   -> "%"
    )
    stringy.toUrlEncoded.shouldBe(
      "apple=http%3A%2F%2Ffoo&bananas=10&pears=%25": String Refined UrlEncoded
    )
  }

  it.should("encode Urls").in {
    val url: String Refined Url =
      "http://foo/?blah=http%3A%2F%2Ffoo&bloo=bar"
    url.toUrlEncoded.shouldBe(
      // the %3A must be double escaped to %253A
      "http%3A%2F%2Ffoo%2F%3Fblah%3Dhttp%253A%252F%252Ffoo%26bloo%3Dbar": String Refined UrlEncoded
    )
  }

  it.should("encode final case classes").in {
    Foo("http://foo", 10L, "%").toUrlEncoded.shouldBe(
      "apple=http%3A%2F%2Ffoo&bananas=10&pears=%25": String Refined UrlEncoded
    )
  }

}
