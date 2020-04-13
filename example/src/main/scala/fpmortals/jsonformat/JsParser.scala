// Copyright: 2017 - 2020 Sam Halliday, 2020 Zara Turtle
// License: https://firstdonoharm.dev/version/2/1/license.html

package fpmortals.jsonformat

import cats._, implicits._

import scala.util.control.NoStackTrace
import org.typelevel.jawn._

object JsParser extends SupportParser[JsValue] {

  // jawn uses exceptions for control flow (the error case), which is a huge DOS
  // security vulnerability, but we can't do anything about it.
  def apply(s: String): Either[String, JsValue] =
    try Right(parseUnsafe(s))
    catch {
      case _: Throwable => Left("invalid json")
    }

  implicit val facade: Facade[JsValue] =
    new Facade.SimpleFacade[JsValue] {
      val jnull: JsNull.type = JsNull
      val jfalse: JsBoolean  = JsBoolean(false)
      val jtrue: JsBoolean   = JsBoolean(true)
      def jnum(cs: CharSequence, decIndex: Int, expIndex: Int): JsValue = {
        val s = cs.toString
        val n =
          if (decIndex == -1)
            s.parseLong.map(JsInteger(_))
          else if (s.endsWith(".0"))
            s.substring(0, s.length - 2).parseLong.map(JsInteger(_))
          else
            s.parseDouble.map(JsDouble(_))
        n.getOrElse(
          throw new IllegalArgumentException(s"bad number $s")
            with NoStackTrace
        )
      }

      def jstring(s: CharSequence): JsString          = JsString(s.toString)
      def jarray(vs: List[JsValue]): JsArray          = JsArray(vs.toIList)
      def jobject(vs: Map[String, JsValue]): JsObject = JsObject(vs.asIList)
    }
}
