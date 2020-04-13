// Copyright: 2017 - 2018 Sam Halliday, 2020 Zara Turtle
// License: https://firstdonoharm.dev/version/2/1/license.html

package fpmortals
package dda
package interpreters

import cats._, implicits._
import algebra._
import http._

final class DroneModule[F[_]](
  H: OAuth2JsonClient[F]
) extends Drone[F] {
  // the bad news is that Drone doesn't currently implement the APIs need for
  // this. I was more or less told about a year ago that this would all be in
  // place, but they changed their mind, and we can't blame them because we're
  // not paying them to do our bidding. If you want to implement this, you'll
  // have to learn Go and implement some simple APIs on the drone server.
  // Sorryz.

  // https://discourse.drone.io/t/build-logs-via-rest/2456
  // http://docs.drone.io/api-overview/

  def getAgents: F[Int]  = ???
  def getBacklog: F[Int] = ???
}
