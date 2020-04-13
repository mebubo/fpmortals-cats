// Copyright: 2020 Zara Turtle
// License: https://firstdonoharm.dev/version/2/1/license.html

package fpmortals.jsonformat

import cats._, implicits._

sealed abstract class JsValue {
  def widen: JsValue = this
}

final case object JsNull                                   extends JsValue
final case class JsObject(fields: List[(String, JsValue)]) extends JsValue
final case class JsArray(elements: List[JsValue])          extends JsValue
final case class JsBoolean(value: Boolean)                 extends JsValue
final case class JsString(value: String)                   extends JsValue
final case class JsDouble(value: Double)                   extends JsValue
final case class JsInteger(value: Long)                    extends JsValue
