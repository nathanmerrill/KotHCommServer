package repository

import javax.inject._
import models.{Challenge, Tournament}
import play.db.ebean.EbeanConfig

import scala.concurrent.Future

class ChallengeRepository @Inject()(val ebeanConfig: EbeanConfig, val executionContext: DatabaseExecutionContext) extends BaseRepository[Challenge] {
  val modelClass: Class[Challenge] = classOf[Challenge]

  def all(): Future[List[Challenge]] = getList {
    query
      .select("name,createdAt")
      .fetch("owner", "id,name")
//      .where().eq("status", Challenge.Status.Active)
      .orderBy("createdAt")
  }

  def view(id: Long): Future[Option[Challenge]] =
    getOne {
      query
        .fetch("entries", "id,currentName")
        .fetch("owner", "id,name")
        .fetch("versions", "id,createdAt,version")
    }(id)

  def update(data: Challenge): Future[Option[Challenge]] = {
    updateModel(data)
  }


  def activeChallenges(): Future[List[Challenge]] = getList {
    query
      .select("id,repoUrl,builder,buildParameters,iterationGoal")
      .fetch("versions", "id,gitHash,matchmaker,gameSize,scorer,scoringParameters")
      .fetch("versions.games", "id,startTime,endTime")
      .where().eq("status", Challenge.Status.Active)
      .orderBy("versions.createdAt")


  }

}
