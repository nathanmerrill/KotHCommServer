package repository

import javax.inject._

import models.Challenge
import play.db.ebean.EbeanConfig

import scala.concurrent.Future

class ChallengeRepository @Inject()(val ebeanConfig: EbeanConfig, val executionContext: DatabaseExecutionContext) extends BaseRepository[Challenge] {
  val modelClass: Class[Challenge] = classOf[Challenge]

  def all(): Future[List[Challenge]] = getList {
    query
      .orderBy("createdAt")
      .select("name,createdAt")
      .fetch("owner", "id,name")
  }

  def view(id: Long): Future[Option[Challenge]] =
    getOne {
      query
        .fetch("entries", "id,currentName")
        .fetch("owner", "id,name")
        .fetch("versions", "id,createdAt,versionId")
    }(id)

  def update(data: Challenge): Future[Option[Challenge]] = {
    val toSave: Challenge = new Challenge
    toSave.id = data.id
    toSave.name = data.name
    toSave.refId = data.refId
    toSave.repoUrl = data.repoUrl
    toSave.owner = data.owner
    updateModel(data)
  }
}
