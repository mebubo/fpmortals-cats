// Copyright: 2017 - 2018 Sam Halliday, 2020 Zara Turtle
// License: https://firstdonoharm.dev/version/2/1/license.html

package fpmortals.prelude

import cats._, implicits._
import cats.effect._

import scala.{ None, Option }

import org.scalactic.source.Position
import org.scalatest.FlatSpec
import org.scalatest.words.ResultOfStringPassedToVerb
import org.scalatest.exceptions.TestFailedException

abstract class Test extends FlatSpec {

  // original book used Eq and Show here but we use Object.{toString,equals}
  // for compatibility.
  implicit final class TestSyntax[A](private val self: A) {
    def shouldBe(
      that: A
    )(implicit P: Position): Unit =
      if (self != that) {
        val msg = s"$that was not equal to $self"
        throw new TestFailedException(
          _ => Option(msg),
          None,
          P
        )
      }
  }

  implicit final class VerkSyntax(v: ResultOfStringPassedToVerb) {
    def inTask[A](ta: IO[A]): Unit = v.in {
      ta.void.unsafeRunSync()
    }
  }

}
object Test {
  def unimplemented: Nothing = scala.sys.error("unimplemented")
}
