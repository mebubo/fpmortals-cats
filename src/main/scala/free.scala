// Copyright: 2020 Zara Turtle
// License: https://firstdonoharm.dev/version/2/1/license.html

package free

import cats._

sealed abstract class Free[S[_], A]
object Free {
  final case class Pure[S[_], A](a: A) extends Free[S, A]
  final case class Suspend[S[_], A](a: S[A]) extends Free[S, A]
  final case class FlatMapped[S[_], B, C](c: Free[S, C], f: C => Free[S, B]) extends Free[S, B]

  type Trampoline[A] = Free[Function0, A]
  implicit val trampoline: Monad[Trampoline] =
    new Monad[Trampoline] {
      def pure[A](a: A): Trampoline[A] = Pure(a)
      def flatMap[A, B](fa: Trampoline[A])(f: A => Trampoline[B]): Trampoline[B] =
        FlatMapped(fa, f)

      def tailRecM[A, B](a: A)(f: A => Trampoline[Either[A, B]]): Trampoline[B] = flatMap(f(a)) {
        case Left(a)  => tailRecM(a)(f)
        case Right(b) => pure(b)
      }
    }

}
object Trampoline {
  import cats.implicits._
  import Free._

  def done[A](a: A): Trampoline[A]    = Pure(a)
  def delay[A](a: =>A): Trampoline[A] = suspend(done(a))

  private val unit: Trampoline[Unit]                = Suspend(() => done(()))
  def suspend[A](a: =>Trampoline[A]): Trampoline[A] = unit >> a
}
