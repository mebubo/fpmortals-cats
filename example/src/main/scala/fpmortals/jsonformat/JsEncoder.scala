// Copyright: 2020 Zara Turtle
// License: https://firstdonoharm.dev/version/2/1/license.html

package fpmortals.jsonformat

import simulacrum._
import cats._, data._, implicits._
import JsEncoder.ops._

@typeclass trait JsEncoder[A] {
  def toJson(obj: A): JsValue
}
object JsEncoder
    extends JsEncoderCats1
    with JsEncoderRefined
    with JsEncoderStdlib1
    with JsEncoderStdlib2 {

  implicit val contravariant: Contravariant[JsEncoder] =
    new Contravariant[JsEncoder] {
      def contramap[A, B](fa: JsEncoder[A])(f: B => A): JsEncoder[B] =
        b => fa.toJson(f(b))
    }

  implicit val jsValue: JsEncoder[JsValue] = identity
  implicit val long: JsEncoder[Long]       = JsInteger(_)
  implicit val double: JsEncoder[Double]   = JsDouble(_)
  implicit val boolean: JsEncoder[Boolean] = JsBoolean(_)
  implicit val string: JsEncoder[String]   = JsString(_)

  implicit val float: JsEncoder[Float]   = double.contramap(_.toDouble)
  implicit val int: JsEncoder[Int]       = long.contramap(_.toLong)
  implicit val short: JsEncoder[Short]   = long.contramap(_.toLong)
  implicit val byte: JsEncoder[Byte]     = long.contramap(_.toLong)
  implicit val unit: JsEncoder[Unit]     = long.contramap(_ => 1)
  implicit val char: JsEncoder[Char]     = string.contramap(_.toString)
  implicit val symbol: JsEncoder[Symbol] = string.contramap(_.name)

}

private[jsonformat] trait JsEncoderCats1 {
  this: JsEncoder.type =>

  implicit def nel[A: JsEncoder]: JsEncoder[NonEmptyList[A]] =
    list[A].contramap(_.toList)
}
private[jsonformat] trait JsEncoderRefined {
  this: JsEncoder.type =>

  import eu.timepit.refined.api.Refined
  implicit def refined[A: JsEncoder, B]: JsEncoder[A Refined B] =
    JsEncoder[A].contramap(_.value)
}
private[jsonformat] trait JsEncoderStdlib1 {
  this: JsEncoder.type =>

  implicit def option[A: JsEncoder]: JsEncoder[Option[A]] = {
    case Some(a) => a.toJson
    case None    => JsNull
  }
  implicit def disjunction[A: JsEncoder, B: JsEncoder]: JsEncoder[Either[A, B]] = {
    case Left(a) => a.toJson
    case Right(b) => b.toJson
  }

  implicit def list[A: JsEncoder]: JsEncoder[List[A]] =
    as => JsArray(as.map(_.toJson))
  implicit def dict[A: JsEncoder]: JsEncoder[Map[String, A]] = { m =>
    val fields = m.toList.map {
      case (k, v) => k -> v.toJson
    }
    JsObject(fields.toList)
  }
}
private[jsonformat] trait JsEncoderStdlib2 {
  this: JsEncoder.type =>

  implicit def iterable[T[a] <: Iterable[a], A: JsEncoder]: JsEncoder[T[A]] =
    list[A].contramap(_.toList)
}
