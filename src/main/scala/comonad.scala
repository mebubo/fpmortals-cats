// Copyright: 2017 Sam Halliday, 2020 Zara Turtle
// License: https://firstdonoharm.dev/version/2/1/license.html
package comonad

import cats._, data._, implicits._

// http://blog.sigfpe.com/2006/12/evaluating-cellular-automata-is.html
final case class Hood[A](lefts: List[A], focus: A, rights: List[A])

object Hood {
  implicit class Ops[A](hood: Hood[A]) {
    def toList: List[A] = hood.lefts.reverse ::: hood.focus :: hood.rights

    def previous: Option[Hood[A]] = hood.lefts match {
      case Nil => None
      case head :: tail =>
        Some(Hood(tail, head, hood.focus :: hood.rights))
    }
    def next: Option[Hood[A]] = hood.rights match {
      case Nil => None
      case head :: tail =>
        Some(Hood(hood.focus :: hood.lefts, head, tail))
    }

    def more(f: Hood[A] => Option[Hood[A]]): List[Hood[A]] =
      f(hood) match {
        case None => Nil
        case Some(r) => r :: r.more(f)
      }
    def positions: Hood[Hood[A]] = {
      val left  = hood.more(_.previous)
      val right = hood.more(_.next)
      Hood(left, hood, right)
    }
  }

  implicit val comonad: Comonad[Hood] = new Comonad[Hood] {
    def map[A, B](fa: Hood[A])(f: A => B): Hood[B] =
      Hood(fa.lefts.map(f), f(fa.focus), fa.rights.map(f))

    // uncomment for performance
    // override def coflatten[A](fa: Hood[A]): Hood[Hood[A]] = fa.positions

    def coflatMap[A, B](fa: Hood[A])(f: Hood[A] => B): Hood[B] =
      fa.positions.map(f)
    def extract[A](fa: Hood[A]): A = fa.focus
  }
}

object example {
  def main(args: Array[String]): Unit = {

    val middle = Hood(List(4, 3, 2, 1), 5, List(6, 7, 8, 9))

    println(middle.coflatten)

    /*
     Hood(
       [Hood([3,2,1],4,[5,6,7,8,9]),
        Hood([2,1],3,[4,5,6,7,8,9]),
        Hood([1],2,[3,4,5,6,7,8,9]),
        Hood([],1,[2,3,4,5,6,7,8,9])],
       Hood([4,3,2,1],5,[6,7,8,9]),
       [Hood([5,4,3,2,1],6,[7,8,9]),
        Hood([6,5,4,3,2,1],7,[8,9]),
        Hood([7,6,5,4,3,2,1],8,[9]),
        Hood([8,7,6,5,4,3,2,1],9,[])])
   */
  }
}
