// Copyright: 2017 - 2018 Sam Halliday, 2020 Zara Turtle
// License: https://firstdonoharm.dev/version/2/1/license.html

package fpmortals
package http.encoding

import cats._, implicits._
import java.util.regex.Pattern
import eu.timepit.refined.api.Validate

/**
 * Evidence that a `String` is valid `application/x-www-form-urlencoded` (i.e.
 * URLEncoder plus equals and ampersand).
 *
 * Note that a urlencoding encoding can be applied multiple times, this only
 * confirms that at least one encoding round has been applied.
 */
sealed abstract class UrlEncoded
object UrlEncoded {
  // WORKAROUND https://github.com/propensive/contextual/issues/21
  private[this] val valid: Pattern =
    Pattern.compile("\\A(\\p{Alnum}++|[-.*_+=&]++|%\\p{XDigit}{2})*\\z")

  implicit def urlValidate: Validate.Plain[String, UrlEncoded] =
    Validate.fromPredicate(
      s => valid.matcher(s).find(),
      identity,
      new UrlEncoded {}
    )
}
