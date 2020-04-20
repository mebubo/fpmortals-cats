// Copyright: 2017 - 2018 Sam Halliday, 2020 Zara Turtle
// License: https://firstdonoharm.dev/version/2/1/license.html

package fpmortals
package dda
package algebra

import cats._, data._, free._, implicits._
import time.Epoch

trait Drone[F[_]] {
  def getBacklog: F[Int]
  def getAgents: F[Int]
}
object Drone {
  sealed abstract class Ast[A]
  case class GetBacklog() extends Ast[Int]
  case class GetAgents()  extends Ast[Int]

  def liftF[F[_]](implicit I: Ast :<: F): Drone[Free[F, ?]] =
    new Drone[Free[F, ?]] {
      def getBacklog: Free[F, Int] = Free.liftF(I.inj(GetBacklog()))
      def getAgents: Free[F, Int]  = Free.liftF(I.inj(GetAgents()))
    }

  def liftA[F[_]](implicit I: Ast :<: F): Drone[FreeApplicative[F, ?]] =
    new Drone[FreeApplicative[F, ?]] {
      def getBacklog: FreeApplicative[F, Int] = FreeApplicative.lift(I.inj(GetBacklog()))
      def getAgents: FreeApplicative[F, Int]  = FreeApplicative.lift(I.inj(GetAgents()))
    }

  def interpreter[F[_]](f: Drone[F]): Ast ~> F = λ[Ast ~> F] {
    case GetBacklog() => f.getBacklog: F[Int]
    case GetAgents()  => f.getAgents: F[Int]
  }
}

final case class MachineNode(id: String)

trait Machines[F[_]] {
  def getTime: F[Epoch]
  def getManaged: F[NonEmptyList[MachineNode]]
  def getAlive: F[Map[MachineNode, Epoch]]
  def start(node: MachineNode): F[Unit]
  def stop(node: MachineNode): F[Unit]
}
object Machines {
  sealed abstract class Ast[A]
  case class GetTime()                extends Ast[Epoch]
  case class GetManaged()             extends Ast[NonEmptyList[MachineNode]]
  case class GetAlive()               extends Ast[Map[MachineNode, Epoch]]
  case class Start(node: MachineNode) extends Ast[Unit]
  case class Stop(node: MachineNode)  extends Ast[Unit]

  def liftF[F[_]](implicit I: Ast :<: F): Machines[Free[F, ?]] =
    new Machines[Free[F, ?]] {
      def getTime: Free[F, Epoch] = Free.liftF(I.inj(GetTime()))
      def getManaged: Free[F, NonEmptyList[MachineNode]] =
        Free.liftF(I.inj(GetManaged()))
      def getAlive: Free[F, Map[MachineNode, Epoch]] =
        Free.liftF(I.inj(GetAlive()))
      def start(node: MachineNode): Free[F, Unit] =
        Free.liftF(I.inj(Start(node)))
      def stop(node: MachineNode): Free[F, Unit] =
        Free.liftF(I.inj(Stop(node)))
    }

  def liftA[F[_]](implicit I: Ast :<: F): Machines[FreeApplicative[F, ?]] =
    new Machines[FreeApplicative[F, ?]] {
      def getTime: FreeApplicative[F, Epoch] = FreeApplicative.lift(I.inj(GetTime()))
      def getManaged: FreeApplicative[F, NonEmptyList[MachineNode]] =
        FreeApplicative.lift(I.inj(GetManaged()))
      def getAlive: FreeApplicative[F, Map[MachineNode, Epoch]] =
        FreeApplicative.lift(I.inj(GetAlive()))
      def start(node: MachineNode): FreeApplicative[F, Unit] =
        FreeApplicative.lift(I.inj(Start(node)))
      def stop(node: MachineNode): FreeApplicative[F, Unit] =
        FreeApplicative.lift(I.inj(Stop(node)))
    }

  def interpreter[F[_]](f: Machines[F]): Ast ~> F = λ[Ast ~> F] {
    case GetTime()    => f.getTime: F[Epoch]
    case GetManaged() => f.getManaged: F[NonEmptyList[MachineNode]]
    case GetAlive()   => f.getAlive: F[Map[MachineNode, Epoch]]
    case Start(node)  => f.start(node): F[Unit]
    case Stop(node)   => f.stop(node): F[Unit]
  }
}
