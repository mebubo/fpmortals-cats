// Copyright: 2017 - 2018 Sam Halliday, 2020 Zara Turtle
// License: https://firstdonoharm.dev/version/2/1/license.html

package fpmortals
package dda
package algebra

import cats._, data._, implicits._
import time.Epoch

trait Drone[F[_]] {
  def getBacklog: F[Int]
  def getAgents: F[Int]
}
object Drone extends DroneBoilerplate

final case class MachineNode(id: String)

trait Machines[F[_]] {
  def getTime: F[Epoch]
  def getManaged: F[NonEmptyList[MachineNode]]
  def getAlive: F[Map[MachineNode, Epoch]]
  def start(node: MachineNode): F[Unit]
  def stop(node: MachineNode): F[Unit]
}
object Machines extends MachinesBoilerlate
