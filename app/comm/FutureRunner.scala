package comm

import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import akka.pattern.ask
import akka.util.Timeout
import comm.FutureActor.TaskToken
import javax.inject.Inject

import scala.concurrent.Future


class FutureRunner @Inject()(system: ActorSystem) {
  implicit val timeout: Timeout = Timeout(10, TimeUnit.SECONDS)

  def run[T](future: Future[T]): Future[TaskToken] = {
    val actor = system.actorOf(FutureActor.props, "future-runner")
    (actor ? future).mapTo[TaskToken]
  }
}
