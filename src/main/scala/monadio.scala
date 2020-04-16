// Copyright: 2020 Zara Turtle
// License: https://firstdonoharm.dev/version/2/1/license.html

package monadio

import scala.io.StdIn
import scala.concurrent.{ Await, ExecutionContext, Future }
import scala.concurrent.duration.Duration

import cats._, implicits._

final class IO[A] private (val interpret: () => A)
object IO {
  def apply[A](a: =>A): IO[A] = new IO(() => a)

  def fail[A](t: Throwable): IO[A] = IO(throw t)

  implicit val Monad: Monad[IO] = new Monad[IO] {
    def pure[A](a: A): IO[A] = IO(a)
    def flatMap[A, B](fa: IO[A])(f: A => IO[B]): IO[B] =
      IO(f(fa.interpret()).interpret())

    def tailRecM[A, B](a: A)(f: A => IO[Either[A, B]]): IO[B] =
      ??? // not tail recursive

    // perf
    // override def map[A, B](fa: IO[A])(f: A => B): IO[B] =
    //   IO(f(fa.interpret()))
  }

}

object Runner {
  import brokenfuture.Terminal
  import brokenfuture.Runner.echo

  implicit val TerminalIO: Terminal[IO] = new Terminal[IO] {
    def read: IO[String]           = IO { StdIn.readLine }
    def write(t: String): IO[Unit] = IO { println(t) }
  }

  val program: IO[String] = echo[IO]

  def forever[F[_]: FlatMap, A](fa: F[A]): F[Unit] =
    fa.flatMap(_ => forever(fa)).void

  def main(args: Array[String]): Unit = {
    // program.interpret()

    val hello = IO { println("hello") }

    forever(hello).interpret()

    // FlatMap[IO].foreverM(hello).interpret()

  }
}
