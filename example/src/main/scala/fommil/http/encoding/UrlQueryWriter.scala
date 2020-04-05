// Copyright: 2017 - 2018 Sam Halliday, 2020 Zara Turtle
// License: https://firstdonoharm.dev/version/2/1/license.html

package fommil
package http.encoding

import prelude._

import java.net.URLDecoder

import shapeless._
import shapeless.labelled._
import simulacrum._

@typeclass trait UrlQueryWriter[A] {
  def toUrlQuery(a: A): UrlQuery
}
trait DerivedUrlQueryWriter[T] extends UrlQueryWriter[T]
object DerivedUrlQueryWriter {
  def gen[T, Repr](
    implicit
    G: LabelledGeneric.Aux[T, Repr],
    CR: Cached[Strict[DerivedUrlQueryWriter[Repr]]]
  ): UrlQueryWriter[T] = { t =>
    CR.value.value.toUrlQuery(G.to(t))
  }

  implicit val hnil: DerivedUrlQueryWriter[HNil] = { _ =>
    UrlQuery(IList.empty)
  }
  implicit def hcons[Key <: Symbol, A, Remaining <: HList](
    implicit Key: Witness.Aux[Key],
    LV: Lazy[UrlEncodedWriter[A]],
    DR: DerivedUrlQueryWriter[Remaining]
  ): DerivedUrlQueryWriter[FieldType[Key, A] :: Remaining] = {
    case head :: tail =>
      val first = {
        val decodedKey = Key.value.name
        // UTF-8 always succeeds
        val decodedValue = URLDecoder.decode(
          LV.value.toUrlEncoded(head).value,
          "UTF-8"
        ) // scalafix:ok
        decodedKey -> decodedValue
      }

      val rest = DR.toUrlQuery(tail)
      UrlQuery(first :: rest.params)
  }
}
