// Copyright: 2020 Zara Turtle
// License: https://firstdonoharm.dev/version/2/1/license.html

package transformers

import cats._, data._, implicits._

import scala.util.control.NoStackTrace

final case class User(name: String) extends AnyVal

trait Twitter[F[_]] {
  def getUser(name: String): F[Option[User]]
  def getStars(user: User): F[Int]
}
object Twitter {
  def T[F[_]](implicit t: Twitter[F]): Twitter[F] = t
}
import Twitter.T

object Difficult {
  def stars[F[_]: Monad: Twitter](name: String): F[Option[Int]] =
    for {
      maybeUser  <- T.getUser(name)
      maybeStars <- maybeUser.traverse(T.getStars)
    } yield maybeStars
}

object WithOptionT {
  def stars[F[_]: Monad: Twitter](name: String): OptionT[F, Int] =
    for {
      user  <- OptionT(T.getUser(name))
      stars <- OptionT.liftF(T.getStars(user))
    } yield stars
}

object WithMonadError {
  def stars[F[_]: Twitter](
    name: String
  )(implicit F: MonadError[F, String]): F[Int] =
    for {
      user <- T.getUser(name).flatMap(F.fromOption(_, s"user '$name' not found"))
      stars <- T.getStars(user)
    } yield stars
}

object WithEitherT {
  def stars[F[_]: Monad: Twitter](name: String): EitherT[F, String, Int] =
    for {
      user  <- EitherT.fromOptionF(T.getUser(name), s"user '$name' not found")
      stars <- EitherT.right(T.getStars(user))
    } yield stars
}

object BetterErrors {
  final case class Meta(fqn: String, file: String, line: Int)
  object Meta {
    implicit def gen(
      implicit fqn: sourcecode.FullName,
      file: sourcecode.File,
      line: sourcecode.Line
    ): Meta =
      new Meta(fqn.value, file.value, line.value)
  }

  final case class Err(msg: String)(implicit val meta: Meta)
      extends Throwable with NoStackTrace
  def main(args: Array[String]) =
    println(Err("hello world").meta)

}

object MockErrors {
  final class MockTwitter extends Twitter[Either[String, ?]] {
    def getUser(name: String): Either[String, Option[User]] =
      if (name.contains(" ")) Right(None)
      else if (name === "wobble") Left("connection error")
      else Right(Some(User(name)))

    def getStars(user: User): Either[String, Int] =
      if (user.name.startsWith("w")) Right(10)
      else Left("stars have been replaced by hearts")
  }

  implicit val twitter: Twitter[Either[String, ?]] = new MockTwitter

  def main(args: Array[String]) = {
    println(WithMonadError.stars("wibble"))
    println(WithMonadError.stars("wobble"))
    println(WithMonadError.stars("i'm a fish"))
    println(WithMonadError.stars("typelevel"))
  }

}
