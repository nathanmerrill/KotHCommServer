package repository

import javax.inject._
import models.Challenge
import play.db.ebean.EbeanConfig

import scala.concurrent.Future

class TournamentRepository @Inject()(val ebeanConfig: EbeanConfig, val executionContext: DatabaseExecutionContext) extends BaseRepository[Challenge] {
  val modelClass: Class[Challenge] = classOf[Challenge]

  def view(id: Long): Future[Option[Challenge]] =
    getOne {
      query
        .fetch("challenge", "id,name")
        .fetch("entries", "id,rank")
        .fetch("entries.version", "id,name")
        .fetch("games", "id,startTime,endTime")
    }(id)
}
