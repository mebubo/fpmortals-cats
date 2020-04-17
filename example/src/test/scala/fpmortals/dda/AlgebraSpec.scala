// Copyright: 2017 - 2018 Sam Halliday, 2020 Zara Turtle
// License: https://firstdonoharm.dev/version/2/1/license.html

package fpmortals
package dda
package algebra

import cats._, data._, implicits._
import cats.effect._
import cats.free._
import cats.data.EitherK.{ leftc, rightc }

import fpmortals.time.Epoch
import fpmortals.prelude.Test
import Test.unimplemented

object Demo {
  def todo[F[_]: Monad](M: Machines[F], D: Drone[F]): F[Int] =
    for {
      work  <- D.getBacklog
      alive <- M.getAlive
    } yield (work - alive.size)

  type Ast[a] = EitherK[Machines.Ast, Drone.Ast, a]
  type Ctx[a] = Free[Ast, a]
  val program: Ctx[Int] = todo[Ctx](Machines.liftF, Drone.liftF)
}

object DummyDrone extends Drone[IO] {
  def getAgents: IO[Int]  = unimplemented
  def getBacklog: IO[Int] = IO(1)
}
object DummyMachines extends Machines[IO] {
  def getAlive: IO[Map[MachineNode, Epoch]]     = IO(Map())
  def getManaged: IO[NonEmptyList[MachineNode]] = unimplemented
  def getTime: IO[Epoch]                        = unimplemented
  def start(node: MachineNode): IO[Unit]        = unimplemented
  def stop(node: MachineNode): IO[Unit]         = unimplemented
}

trait Batch[F[_]] {
  def start(nodes: NonEmptyList[MachineNode]): F[Unit]
}
object Batch {
  sealed abstract class Ast[A]
  final case class Start(nodes: NonEmptyList[MachineNode]) extends Ast[Unit]
  def liftF[F[_]](implicit I: Ast :<: F): Batch[Free[F, ?]] =
    new Batch[Free[F, ?]] {
      def start(nodes: NonEmptyList[MachineNode]): Free[F, Unit] =
        Free.liftF(I.inj(Start(nodes)))
    }
  // def liftA[F[_]](implicit I: Ast :<: F): Batch[FreeAp[F, ?]] =
  //   new Batch[FreeAp[F, ?]] {
  //     def start(nodes: NonEmptyList[MachineNode]): FreeAp[F, Unit] =
  //       FreeAp.lift(I.inj(Start(nodes)))
  //   }
}

object Monkeys {
  final case class S(
    singles: List[MachineNode],
    batches: List[NonEmptyList[MachineNode]]
  ) {
    def addSingle(node: MachineNode): S =
      S(node :: singles, batches)
    def addBatch(nodes: NonEmptyList[MachineNode]): S =
      S(singles, nodes :: batches)
  }
}

final class AlgebraSpec extends Test {

  // https://github.com/scalaz/scalaz/pull/1753
  def or[F[_], G[_], H[_]](fg: F ~> G, hg: H ~> G): EitherK[F, H, ?] ~> G =
    λ[EitherK[F, H, ?] ~> G](_.fold(fg, hg))

  "Free Algebra Interpreters".should("combine their powers").in {
    val iD: Drone.Ast ~> IO         = Drone.interpreter(DummyDrone)
    val iM: Machines.Ast ~> IO      = Machines.interpreter(DummyMachines)
    val interpreter: Demo.Ast ~> IO = iM or iD

    Demo.program
      .foldMap(interpreter)
      .unsafeRunSync()
      .shouldBe(1)
  }

  it.should("support monitoring").in {
    val iD: Drone.Ast ~> IO         = Drone.interpreter(DummyDrone)
    val iM: Machines.Ast ~> IO      = Machines.interpreter(DummyMachines)
    val interpreter: Demo.Ast ~> IO = iM or iD

    var count = 0
    val Monitor = λ[Demo.Ast ~> Demo.Ast](
      _.run match {
        case Right(m @ Drone.GetBacklog()) =>
          count += 1
          EitherK.rightc(m)
        case other =>
          EitherK(other)
      }
    )

    Demo.program
      .mapK(Monitor)
      .foldMap(interpreter)
      .unsafeRunSync()
      .shouldBe(1)

    count.shouldBe(1)
  }

  it.should("allow smocking").in {
    import Mocker._

    val D: Drone.Ast ~> Id = stub[Int] {
      case Drone.GetBacklog() => 1
    }
    val M: Machines.Ast ~> Id = stub[Map[MachineNode, Epoch]] {
      case Machines.GetAlive() => Map()
    }

    Demo.program
      .foldMap(M or D)
      .shouldBe(1)
  }

  it.should("support monkey patching part 1").in {
    type S = Set[MachineNode]
    val M = Mocker.stubAny[Machines.Ast, State[S, ?]] {
      case Machines.Stop(node) => State.modify[S](_ + node)
    }

    val monkey = λ[Machines.Ast ~> Free[Machines.Ast, ?]] {
      case Machines.Stop(MachineNode("#c0ffee")) => Free.pure(())
      case other                                 => Free.liftF(other)
    }

    Machines
      .liftF[Machines.Ast]
      .stop(MachineNode("#c0ffee"))
      .foldMap(monkey)
      .foldMap(M)
      .runS(Set())
      .value
      .shouldBe(Set())
  }

  it.should("batch calls without any crazy hacks").in {
    type Orig[a] = EitherK[Machines.Ast, Drone.Ast, a]

    // pretend this is the DynAgents.act method...
    def act[F[_]: Applicative](M: Machines[F], D: Drone[F])(
      todo: Int
    ): F[Unit] =
      (1 to todo).toList.traverse(id => M.start(MachineNode(id.toString))).void

    val freeap = act(Machines.liftA[Orig], Drone.liftA[Orig])(2)

    val gather = λ[Orig ~> λ[α => List[MachineNode]]] {
      case EitherK(Left(Machines.Start(node))) => List(node)
      case _                                   => Nil
    }

    type Extended[a] = EitherK[Batch.Ast, Orig, a]
    def batch(nodes: List[MachineNode]): FreeApplicative[Extended, Unit] =
      nodes.toNel match {
        case None        => FreeApplicative.pure(())
        case Some(nodes) => FreeApplicative.lift(EitherK.leftc(Batch.Start(nodes)))
      }

    val nostart = λ[Orig ~> FreeApplicative[Extended, ?]] {
      case EitherK(Left(Machines.Start(_))) => FreeApplicative.pure(())
      case other                            => FreeApplicative.lift(EitherK.rightc(other))
    }

    def optimise[A](orig: FreeApplicative[Orig, A]): FreeApplicative[Extended, A] =
      (batch(orig.analyze(gather)) *> orig.foldMap(nostart))

    import Monkeys.S
    type T[a] = State[S, a]
    val M: Machines.Ast ~> T = Mocker.stub[Unit] {
      case Machines.Start(node) => State.modify[S](_.addSingle(node))
    }
    val D: Drone.Ast ~> T = Mocker.stub[Int] {
      case Drone.GetBacklog() => 2.pure[T]
    }
    val B: Batch.Ast ~> T = Mocker.stub[Unit] {
      case Batch.Start(nodes) => State.modify[S](_.addBatch(nodes))
    }

    optimise(freeap)
      .foldMap(B or (M or D))
      .runS(S(List(), List()))
      .value
      .shouldBe(
        S(
          List(), // no singles
          List(NonEmptyList.of(MachineNode("1"), MachineNode("2")))
        )
      )

  }
}

// inspired by https://github.com/djspiewak/smock
object Mocker {
  import scala.PartialFunction
  final class Stub[A] {
    def apply[F[_], G[_]](pf: PartialFunction[F[A], G[A]]): F ~> G =
      new (F ~> G) {
        override def apply[α](fa: F[α]): G[α] =
          // safe because F and G share a type parameter
          pf.asInstanceOf[PartialFunction[F[α], G[α]]](fa)
      }
  }
  def stub[A]: Stub[A] = new Stub[A]

  // an even less safe variant, but allows more than one return type. The
  // partially applied trick doesn't seem to work.
  def stubAny[F[_], G[_]](pf: PartialFunction[F[_], G[_]]): F ~> G =
    new (F ~> G) {
      override def apply[α](fa: F[α]): G[α] =
        // not even nearly safe, but we'll catch mistakes when the test runs
        pf.asInstanceOf[PartialFunction[F[α], G[α]]](fa)
    }
}
