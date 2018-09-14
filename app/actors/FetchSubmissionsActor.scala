package actors

import akka.actor.Actor
import comm.Downloader
import javax.inject.Inject
import models.Challenge

import scala.util.Try

class FetchSubmissionsActor @Inject()(downloader: Downloader) extends Actor {
  def receive: PartialFunction[Any, Unit] = {
    case challenge: Challenge => {
      if (Try(Integer.parseInt(challenge.refId)).isSuccess) {
        downloader.downloadQuestions(challenge.refId)
      }
    }

    case _ => println("Error: message not recognized")
  }

}
