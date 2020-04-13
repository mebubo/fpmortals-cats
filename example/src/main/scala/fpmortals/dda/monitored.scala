// Copyright: 2017 - 2018 Sam Halliday, 2020 Zara Turtle
// License: https://firstdonoharm.dev/version/2/1/license.html

package fpmortals
package dda
package logic

import cats._, data._, implicits._

import algebra._
import time.Epoch

final class Monitored[U[_]: Functor](program: DynAgents[U]) {
  type F[a] = Const[Set[MachineNode], a]

  val D: Drone[F] = new Drone[F] {
    def getBacklog: F[Int] = Const(Set())
    def getAgents: F[Int]  = Const(Set())
  }

  val M: Machines[F] = new Machines[F] {
    def getAlive: F[Map[MachineNode, Epoch]]     = Const(Set())
    def getManaged: F[NonEmptyList[MachineNode]] = Const(Set())
    def getTime: F[Epoch]                        = Const(Set())
    def start(node: MachineNode): F[Unit]        = Const(Set())
    def stop(node: MachineNode): F[Unit]         = Const(Set(node))
  }

  val monitor: DynAgents[F] = new DynAgentsModule[F](D, M)

  def act(world: WorldView): U[(WorldView, Set[MachineNode])] = {
    val stopped = monitor.act(world).getConst
    program.act(world).tupleRight(stopped)
  }

}
