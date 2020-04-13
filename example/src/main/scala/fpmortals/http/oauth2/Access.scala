// Copyright: 2017 - 2018 Sam Halliday, 2020 Zara Turtle
// License: https://firstdonoharm.dev/version/2/1/license.html

package fpmortals
package http
package oauth2

import cats._, implicits._

import scala.concurrent.duration._
import time._
import http.JsonClient
import api._

/**
 * Refresh tokens do not expire, except in response to a security
 * breach or user / server whim.
 */
final case class RefreshToken(token: String) extends AnyVal

/**
 * Turns a one-shot `CodeToken` into a `BearerToken` than can be securely
 * persisted, and a `RefreshToken` that can be used temporarily.
 */
trait Access[F[_]] {
  def access(code: CodeToken): F[(RefreshToken, BearerToken)]
}

final class AccessModule[F[_]: Monad](
  config: ServerConfig
)(
  H: JsonClient[F],
  T: LocalClock[F]
) extends Access[F] {

  def access(code: CodeToken): F[(RefreshToken, BearerToken)] =
    for {
      request <- AccessRequest(
                  code.token,
                  code.redirect_uri,
                  config.clientId,
                  config.clientSecret
                ).pure[F]
      msg <- H.post[AccessRequest, AccessResponse](
              config.access,
              request,
              Nil
            )
      time    <- T.now
      expires = time + msg.expires_in.seconds
      refresh = RefreshToken(msg.refresh_token)
      bearer  = BearerToken(msg.access_token, expires)
    } yield (refresh, bearer)

}
