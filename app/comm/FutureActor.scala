package comm

import akka.actor.{Actor, Props}
import comm.FutureActor.ActorStatus.ActorStatus

import scala.concurrent.Future
import scala.util.{Failure, Random, Success}
import scala.concurrent.ExecutionContext.Implicits.global

object FutureActor {
  def props: Props = Props[FutureActor]
  object ActorStatus extends Enumeration {
    type ActorStatus = Value
    val Running, Finished, Error = Value
  }

  case class TaskToken(id: Int)
}

class FutureActor extends Actor {
  import FutureActor._

  var map : Map[Int, (ActorStatus, String)] = Map()

  def receive: PartialFunction[Any, Unit] = {
    case future: Future[String] =>
      val id: Int = Random.nextInt()
      map = map + (id -> (ActorStatus.Running, ""))
      future.andThen {
        case Success(result) => map += (id -> (ActorStatus.Finished, result))
        case Failure(message) => map += (id -> (ActorStatus.Error, message.getMessage))
      }
      sender() ! TaskToken(id)

    case TaskToken(id) => sender() ! map.get(id).map(_._1)
  }
}
