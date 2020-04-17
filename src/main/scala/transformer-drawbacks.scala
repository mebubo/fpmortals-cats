// Copyright: 2020 Zara Turtle
// License: https://firstdonoharm.dev/version/2/1/license.html
package transformerdrawbacks

import cats._, data._, arrow._, implicits._
import cats.effect._

final case class Problem(bad: Int)
final case class Table(last: Int)

trait Lookup[F[_]] { self =>
  def look: F[Int]

  def mapK[G[_]](f: F ~> G): Lookup[G] = new Lookup[G] {
    def look: G[Int] = f(self.look)
  }

}

object LookupRandom extends Lookup[IO] {
  def look: IO[Int] = IO { scala.util.Random.nextInt }
}

// trait MonadErrorState[F[_], E, S] {
//   implicit def E: MonadError[F, E]
//   implicit def S: MonadState[F, S]
// }
// object MonadErrorState {
//   implicit def create[F[_], E, S](
//     implicit E0: MonadError[F, E],
//     S0: MonadState[F, S]
//   ): MonadErrorState[F, E, S] =
//     new MonadErrorState[F, E, S] {
//       def E: MonadError[F, E] = E0
//       def S: MonadState[F, S] = S0
//     }
// }

object Logic {
  type Ctx[A] = StateT[EitherT[IO, Problem, ?], Table, A]

  def liftK: IO ~> Ctx = {
    type Ctx1[A] = EitherT[IO, Problem, A]
    val first : IO ~> Ctx1 = EitherT.liftK
    val second : Ctx1 ~> Ctx = StateT.liftK
    second.compose(first)
  }

  val lookup1: Lookup[Ctx] = LookupRandom.mapK(liftK)

  def liftIoK[F[_]](implicit L: LiftIO[F]): IO ~> F = FunctionK.lift(L.liftIO)

  val lookup2: Lookup[Ctx] = LookupRandom.mapK(liftIoK)

  // implicit class CtxOps[A](fa: IO[A]) {
  //   def liftCtx: Ctx[A] =
  //     fa.liftM[EitherT[?[_], Problem, ?]]
  //       .liftM[StateT[?[_], Table, ?]]
  // }

  // type Ctx0[F[_], A] = StateT[EitherT[F, Problem, ?], Table, A]
  // type Ctx1[F[_], A] = EitherT[F, Problem, A]
  // type Ctx2[F[_], A] = StateT[F, Table, A]
  // final class LookupIOCtx(io: Lookup[IO]) extends Lookup[Ctx] {
  //   def look1: Ctx[Int] = io.look.liftM[Ctx1].liftM[Ctx2]

  //   def look2: Ctx[Int] =
  //     io.look
  //       .liftM[EitherT[?[_], Problem, ?]]
  //       .liftM[StateT[?[_], Table, ?]]

  //   def look3: Ctx[Int] = io.look.liftCtx

  //   def look: Ctx[Int] = io.look.liftIO[Ctx]
  // }

  // final class LookupMonadIO[F[_]: MonadIO](io: Lookup[IO]) extends Lookup[F] {
  //   def look: F[Int] = io.look.liftIO[F]
  // }

  // val wrap1: Lookup[EitherT[IO, Problem, ?]] =
  //   Lookup.liftM[IO, Ctx1](LookupRandom)
  // //new LookupTrans[IO, Ctx1](LookupRandom)

  // val wrap2: Lookup[Ctx] = Lookup.liftM[EitherT[IO, Problem, ?], Ctx2](wrap1)
  // //new LookupTrans[EitherT[IO, Problem, ?], Ctx2](wrap1)

  // //val wrap: Lookup[Ctx] =
  // //  new LookupTrans[IO, Ctx0](LookupRandom)

  // def foo[F[_]: Monad](L: Lookup[F])(
  //   implicit E: MonadError[F, Problem],
  //   S: MonadState[F, Table]
  // ): F[Int] = ???

  // // implicit Monad, rest explicit. Easier for us, more work for upstream
  // def foo1[F[_]: Monad](L: Lookup[F])(
  //   E: MonadError[F, Problem],
  //   S: MonadState[F, Table]
  // ): F[Int] =
  //   for {
  //     old <- S.get
  //     i   <- L.look
  //     _ <- if (i === old.last) E.raiseError(Problem(i))
  //         else ().pure[F]
  //   } yield i

  // // shadow the parameters
  // def foo2[F[_]: Monad](L: Lookup[F])(
  //   implicit
  //   E: MonadError[F, Problem],
  //   S: MonadState[F, Table]
  // ): F[Int] = shadow(E, S) { (E, S) =>
  //   for {
  //     old <- S.get
  //     i   <- L.look
  //     _ <- if (i === old.last) E.raiseError(Problem(i))
  //         else ().pure[F]
  //   } yield i
  // }

  // @inline final def shadow[A, B](a: A)(f: A => B): B               = f(a)
  // @inline final def shadow[A, B, C](a: A, b: B)(f: (A, B) => C): C = f(a, b)

  // // custom Monad
  // def foo3[F[_]: Monad](L: Lookup[F])(
  //   implicit M: MonadErrorState[F, Problem, Table]
  // ): F[Int] =
  //   for {
  //     old <- M.S.get
  //     i   <- L.look
  //     _ <- if (i === old.last) M.E.raiseError(Problem(i))
  //         else ().pure[F]
  //   } yield i

  // def main(args: Array[String]): Unit = {
  //   val L: Lookup[Ctx] = new LookupIOCtx(LookupRandom)

  //   foo[Ctx](L)

  //   foo2[Ctx](L)

  //   foo3[Ctx](L)

  //   val L2: Lookup[Ctx] = new LookupMonadIO(LookupRandom)

  //   val L3: Lookup[Ctx] = Lookup.liftIO(LookupRandom)
  // }

}
