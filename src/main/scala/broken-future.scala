// Copyright: 2017 Sam Halliday, 2020 Zara Turtle
// License: https://firstdonoharm.dev/version/2/1/license.html
package brokenfuture

import scala.io.StdIn

import scalaz._, Scalaz._

import scala.concurrent._
import scala.concurrent.duration.Duration

trait Terminal[C[_]] {
  def read: C[String]
  def write(t: String): C[Unit]
}

class TerminalAsync(implicit EC: ExecutionContext) extends Terminal[Future] {
  def read: Future[String]           = Future { StdIn.readLine }
  def write(t: String): Future[Unit] = Future { println(t) }
}

object Runner {

  def echo[C[_]: Monad](implicit T: Terminal[C]): C[String] =
    for {
      in <- T.read
      _  <- T.write(in)
    } yield in

  import ExecutionContext.Implicits._
  implicit val future: Terminal[Future] = new TerminalAsync

  def main(args: Array[String]): Unit = {
    // interpret for Future
    val running: Future[String] = echo[Future]

    Await.result(running, Duration.Inf)
  }
}
