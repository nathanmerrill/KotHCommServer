package comm

import akka.actor.ActorSystem
import comm.FutureRunner.TaskToken
import javax.inject.Inject

import scala.concurrent.Future



object FutureRunner {
  case class TaskToken(id: Int)
  object Status extends Enumeration {
    type Status = Value
    val Running, Finished, Error = Value
  }
}

class FutureRunner @Inject()(system: ActorSystem) {
  def run[T](future: Future[T]): TaskToken = {

  }


}
