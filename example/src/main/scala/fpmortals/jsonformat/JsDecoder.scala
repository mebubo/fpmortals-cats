// Copyright: 2020 Zara Turtle
// License: https://firstdonoharm.dev/version/2/1/license.html

package fpmortals.jsonformat

import simulacrum._
import cats._, data._, implicits._

import JsDecoder.ops._

@typeclass(generateAllOps = false) trait JsDecoder[A] {
  def fromJson(json: JsValue): Either[String, A]

  def emap[B](f: A => Either[String, B]): JsDecoder[B] =
    j => fromJson(j).flatMap(f)
}
object JsDecoder
    extends JsDecoderCats1
    with JsDecoderRefined
    with JsDecoderStdlib1
    with JsDecoderStdlib2 {
  object ops {
    implicit final class JsValueExtras(private val j: JsValue) extends AnyVal {
      def as[A: JsDecoder]: Either[String, A] = JsDecoder[A].fromJson(j)
    }
  }

  @inline final def instance[A](f: JsValue => Either[String, A]): JsDecoder[A] = f(_)

  implicit val functor: Functor[JsDecoder] = new Functor[JsDecoder] {
    def map[A, B](fa: JsDecoder[A])(f: A => B): JsDecoder[B] =
      j => fa.fromJson(j).map(f)
  }

  def fail[A](expected: String, got: JsValue): Left[String, Nothing] =
    Left(s"expected $expected, got $got")

  implicit val jsValue: JsDecoder[JsValue] = Right(_)
  implicit val long: JsDecoder[Long] = {
    case JsInteger(n) => Right(n)
    case other        => fail("JsInteger", other)
  }
  implicit val double: JsDecoder[Double] = {
    case JsDouble(n)  => Right(n)
    case JsInteger(n) => Right(n.toDouble) // potential loss of precision
    case other        => fail("JsDouble or JsInteger", other)
  }
  implicit val boolean: JsDecoder[Boolean] = {
    case JsBoolean(x) => Right(x)
    case other        => fail("JsBoolean", other)
  }
  implicit val string: JsDecoder[String] = {
    case JsString(x) => Right(x)
    case other       => fail("JsString", other)
  }

  implicit val float: JsDecoder[Float] = double.emap {
    case n if n >= Float.MinValue && n <= Float.MaxValue => Right(n.toFloat)
    case other                                           => fail("64 bit floating point number", JsDouble(other))
  }
  implicit val int: JsDecoder[Int] = long.emap {
    case n if n >= Int.MinValue && n <= Int.MaxValue => Right(n.toInt)
    case other                                       => fail("32 bit integer", JsInteger(other))
  }
  implicit val short: JsDecoder[Short] = long.emap {
    case n if n >= Short.MinValue && n <= Short.MaxValue => Right(n.toShort)
    case other                                           => fail("16 bit integer", JsInteger(other))
  }
  implicit val byte: JsDecoder[Byte] = long.emap {
    case n if n >= Byte.MinValue && n <= Byte.MaxValue => Right(n.toByte)
    case other                                         => fail("8 bit integer", JsInteger(other))
  }
  implicit val unit: JsDecoder[Unit] = long.emap {
    case 1     => Right(())
    case other => fail("1.0", JsInteger(other))
  }
  implicit val char: JsDecoder[Char] = string.emap {
    case str if str.length == 1 => Right(str(0))
    case other                  => fail("single character", JsString(other))
  }
  implicit val symbol: JsDecoder[Symbol] = string.map(Symbol(_))

}

private[jsonformat] trait JsDecoderCats1 {
  this: JsDecoder.type =>

  implicit def nel[A: JsDecoder]: JsDecoder[NonEmptyList[A]] =
    list[A].emap { lst => lst.toNel match {
        case None => Left("empty list")
        case Some(nel) => Right(nel)
      }
    }
}
private[jsonformat] trait JsDecoderRefined {
  this: JsDecoder.type =>

  import eu.timepit.refined.refineV
  import eu.timepit.refined.api._
  implicit def refined[A: JsDecoder, P](
    implicit V: Validate[A, P]
  ): JsDecoder[A Refined P] =
    JsDecoder[A].emap(refineV[P](_))

}
private[jsonformat] trait JsDecoderStdlib1 {
  this: JsDecoder.type =>

  implicit def option[A: JsDecoder]: JsDecoder[Option[A]] = {
    case JsNull => Right(None)
    case a      => a.as[A].map(Some(_))
  }
  implicit def either[A: JsDecoder, B: JsDecoder]: JsDecoder[Either[A, B]] = {
    v =>
      (v.as[A], v.as[B]) match {
        case (Right(_), Right(_)) => fail("No ambiguity", v)
        case (Left(ea), Left(eb)) => Left(s"Left: ${ea}\nRight: ${eb}")
        case (left, Left(_))      => left.map(Left(_))
        case (Left(_), right)     => right.map(Right(_))
      }
  }

  implicit def dict[A: JsDecoder]: JsDecoder[Map[String, A]] = {
    case JsObject(fields) =>
      fields.traverse {
        case (key, value) => value.as[A].tupleLeft(key)
      }.map(_.toList.toMap)
    case other => fail("JsObject", other)
  }

}
private[jsonformat] trait JsDecoderStdlib2 {
  this: JsDecoder.type =>

  implicit def list[A: JsDecoder]: JsDecoder[List[A]] = {
    case JsArray(js) =>
      val A = JsDecoder[A]
      js.traverse(A.fromJson)
    case other => fail("JsArray", other)
  }

}
