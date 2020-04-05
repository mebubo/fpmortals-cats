// Copyright: 2018 Sam Halliday, 2020 Zara Turtle
// License: https://firstdonoharm.dev/version/2/1/license.html

package fommil
package http
package interpreters

import prelude._, Z._

import jsonformat._
import eu.timepit.refined.string.Url
import pureconfig.orphans._

@deriving(ConfigReader)
final case class JsonServer(jsonUrl: String Refined Url)

@deriving(Equal, Show, JsDecoder)
final case class JsonServerResponse(greetings: String)

final class BlazeJsonClientTest extends Test {

  "blaze client".should("receive json in response to GET").inTask {
    for {
      config <- readConfig[JsonServer]
      client <- BlazeJsonClient()
      response <- client
                   .get[JsonServerResponse](config.jsonUrl, IList.empty)
                   .run
                   .swallowError
    } yield {
      response.shouldBe(JsonServerResponse("hello"))
    }
  }

  // tests for headers, errors and post would be good... requires server side
  // support.

}
