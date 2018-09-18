package comm

import akka.actor.ActorSystem
import akka.pattern.ask
import comm.FutureActor.TaskToken
import javax.inject.Inject

import scala.concurrent.Future


class FutureRunner @Inject()(system: ActorSystem) {
  def run[T](future: Future[T]): Future[TaskToken] = {
    val actor = system.actorOf(FutureActor.props, "future-runner")
    (actor ? future).mapTo[TaskToken]
  }
}
