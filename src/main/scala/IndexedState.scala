// Copyright: 2020 Zara Turtle
// License: https://firstdonoharm.dev/version/2/1/license.html
package indexed

import cats._, data._, implicits._

// based on https://youtu.be/JPVagd9W4Lo?t=928
import Cache._

trait Cache[M[_]] {
  type F[in, out, a] = IndexedStateT[M, in, out, a]

  def read[S <: Status](k: Int): F[S, S, Option[String]]
  //def read[S <: Status](k: Int)(implicit NN: NotNothing[S]): F[S, S, Option[String]]
  // def read(k: Int): F[Ready, Ready, Option[String]]
  // def readLocked(k: Int): F[Locked, Locked, Option[String]]
  // def readUncommitted(k: Int): F[Updated, Updated, Option[String]]

  def lock: F[Ready, Locked, Unit]
  def update(k: Int, v: String): F[Locked, Updated, Unit]
  def commit: F[Updated, Ready, Unit]
}
object Cache {
  sealed abstract class Status
  final case class Ready()                           extends Status
  final case class Locked(on: Set[Int])              extends Status
  final case class Updated(values: Map[Int, String]) extends Status
}

object Main {

  def wibbleise[M[_]: Monad](
    C: Cache[M]
  ): IndexedStateT[M, Ready, Ready, String] =
    for {
      _  <- C.lock
      a1 <- C.read(13)
      a2 = a1 match {
        case None    => "wibble"
        case Some(a) => a + "'"
      }
      _  <- C.update(13, a2)
      _  <- C.commit
    } yield a2

  def fail[M[_]: Monad](
    C: Cache[M]
  ): IndexedStateT[M, Locked, Ready, Option[String]] =
    for {
      a1 <- C.read(13)
      _  <- C.update(13, "wibble")
      _  <- C.commit
    } yield a1
}
