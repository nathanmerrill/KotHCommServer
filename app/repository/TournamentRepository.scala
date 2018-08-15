package repository

import javax.inject._
import models.{Challenge, Tournament}
import play.db.ebean.EbeanConfig

import scala.concurrent.Future

class TournamentRepository @Inject()(val ebeanConfig: EbeanConfig, val executionContext: DatabaseExecutionContext) extends BaseRepository[Tournament] {
  val modelClass: Class[Tournament] = classOf[Tournament]

  def view(id: Long): Future[Option[Tournament]] =
    getOne {
      query
        .fetch("challenge", "id,name")
        .fetch("entries", "id,rank")
        .fetch("entries.version", "id,name")
        .fetch("games", "id,startTime,endTime")
    }(id)

  def all(): Future[List[Tournament]] = getList {
    query
      .orderBy("createdAt")
      .select("name,createdAt")
      .fetch("owner", "id,name")
  }
}
