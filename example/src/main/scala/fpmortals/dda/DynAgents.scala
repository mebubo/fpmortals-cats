// Copyright: 2017 - 2018 Sam Halliday, 2020 Zara Turtle
// License: https://firstdonoharm.dev/version/2/1/license.html

package fpmortals
package dda
package logic

import cats._, data._, implicits._

import scala.concurrent.duration._
import algebra._
import time.Epoch

/**
 * @param backlog how many builds are waiting to be run on the ci
 * @param agents how many agents are fulfilling ci jobs
 * @param managed nodes that are available
 * @param alive nodes are currently alive, values are when they were
 *               started
 * @param pending nodes that we have recently changed the world of.
 *                These should be considered "unavailable". NOTE: we
 *                have a visibility problem here if we never hear back
 *                when they change world to available / alive. Values
 *                are when we requested a change of world.
 * @param time is the most recent clock tick from the service managing
 *             the nodes. Note that this is stale by definition, but
 *             there are best endeavours to refresh it regularly.
 */
final case class WorldView(
  backlog: Int,
  agents: Int,
  managed: NonEmptyList[MachineNode],
  alive: Map[MachineNode, Epoch],
  pending: Map[MachineNode, Epoch],
  time: Epoch
)

trait DynAgents[F[_]] { self =>
  def initial: F[WorldView]
  def update(old: WorldView): F[WorldView]
  def act(world: WorldView): F[WorldView]

  def mapK[G[_]](f: F ~> G): DynAgents[G] = new DynAgents[G] {
    def initial: G[WorldView] = f(self.initial)
    def update(old: WorldView): G[WorldView] = f(self.update(old))
    def act(world: WorldView): G[WorldView] = f(self.act(world))
  }
}

final class DynAgentsModule[F[_]: Applicative](D: Drone[F], M: Machines[F])
    extends DynAgents[F] {

  def initial: F[WorldView] =
    (D.getBacklog, D.getAgents, M.getManaged, M.getAlive, M.getTime).mapN {
      case (db, da, mm, ma, mt) => WorldView(db, da, mm, ma, Map(), mt)
    }

  def update(old: WorldView): F[WorldView] =
    initial.map { snap =>
      val changed = symdiff(old.alive.keySet, snap.alive.keySet)
      val pending = (old.pending -- changed).filter {
            case (_, started) => snap.time - started < 10.minutes
          }
      snap.copy(pending = pending)
    }

  private def symdiff[T](a: Set[T], b: Set[T]): Set[T] =
    (a union b) -- (a intersect b)

  def act(world: WorldView): F[WorldView] = world match {
    case NeedsAgent(node) =>
      M.start(node) as world.copy(pending = Map(node -> world.time))

    case Stale(nodes) =>
      nodes.traverse { node =>
        M.stop(node) as node
      }.map { stopped =>
        val updates = stopped.tupleRight(world.time).toList.toMap
        world.copy(pending = world.pending ++ updates)
      }

    case _ => world.pure[F]
  }

  // with a backlog, but no agents or pending nodes, start a node
  private object NeedsAgent {
    def unapply(world: WorldView): Option[MachineNode] = world match {
      case WorldView(backlog, 0, managed, alive, pending, _)
          if backlog > 0 && alive.isEmpty && pending.isEmpty =>
        Option(managed.head)
      case _ => None
    }
  }

  // when there is no backlog, stop all alive nodes. However, since
  // Google / AWS charge per hour we only shut down machines in their
  // 58th+ minute.
  //
  // Assumes that we are called fairly regularly otherwise we may miss
  // this window (should we take control over calling getTime instead
  // of expecting it from the WorldView we are handed?).
  //
  // Safety net: even if there is a backlog, stop older agents.
  private object Stale {
    def unapply(world: WorldView): Option[NonEmptyList[MachineNode]] =
      world match {
        case WorldView(backlog, _, _, alive, pending, time) if !alive.isEmpty =>
          (alive -- pending.keySet)
            .filter { case (n, started) =>
              (backlog == 0 && (time - started).toMinutes % 60 >= 58) ||
              (time - started >= 5.hours)
            }
            .keys
            .toList
            .toNel

        case _ => None
      }
  }

}
