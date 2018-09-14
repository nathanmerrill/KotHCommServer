package comm

import akka.actor.Actor
import comm.FutureRunner.Status.Status

import scala.concurrent.Future


class FutureActor extends Actor {

  def map : Map[Int, (Status, String)]

  def receive: PartialFunction[Any, Unit] = {
    case future: Future[Any] =>
      future.
  }
}
