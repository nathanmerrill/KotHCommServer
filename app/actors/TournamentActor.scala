package actors

import akka.actor._
import javax.inject.Inject

object TournamentActor {
  def props = Props[TournamentActor]


  case class FetchController()
  case class BuildController()
  case class FetchEntries()
  case class BuildEntries()
  case class BuildEntry()

}

class TournamentActor @Inject() () extends Actor {
  import TournamentActor._

  def receive = {
    case BuildController() =>
      sender() ! "Hello, " + name
  }
}
