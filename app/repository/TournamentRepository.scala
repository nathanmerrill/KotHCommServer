package repository

import javax.inject._
import models.Tournament
import play.db.ebean.EbeanConfig

import scala.concurrent.Future

class TournamentRepository @Inject()(val ebeanConfig: EbeanConfig, val executionContext: DatabaseExecutionContext) extends BaseRepository[Tournament] {


  val modelClass: Class[Tournament] = classOf[Tournament]

  def view(id: Long): Future[Option[Tournament]] =
    getOneWhere(id) {
      query
        .fetch("challenge", "id,name,status")
        .fetch("challenge.owner", "id,")
        .fetch("challenge.activeTournament", "id")
        .fetch("entries", "id,rank")
        .fetch("entries.version", "id,name")
        .fetch("games", "id,startTime,endTime")
    }

  def all(challengeId: Long): Future[List[Tournament]] =
    getList {
      query
        .where().eq("challenge.id", challengeId)
        .orderBy("createdAt")
        .select("name,createdAt")
        .fetch("owner", "id,name")
    }

  def count(challengeId: Long): Future[Int] =
    execute {
      query.where().eq("challenge.id", challengeId).findCount()
    }

}
