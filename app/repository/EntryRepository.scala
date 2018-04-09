package repository

import javax.inject._
import models.Entry
import play.db.ebean.EbeanConfig

import scala.concurrent.Future

/**
  * A repository that executes database operations in a different
  * execution context.
  */
class EntryRepository @Inject()(val ebeanConfig: EbeanConfig, val executionContext: DatabaseExecutionContext) extends BaseRepository[Entry] {
  val modelClass: Class[Entry] = classOf[Entry]


  def view(id: Long): Future[Option[Entry]] =
    getOne {
      query
        .fetch("versions", "id,name,createdAt")
        .fetch("owner", "id,name")
        .fetch("versions", "id,createdAt,versionId")
    }(id)

  def update(data: Entry): Future[Option[Entry]] = {
    val toSave: Entry = new Entry
    toSave.id = data.id
    toSave.currentName = data.currentName
    toSave.refId = data.refId
    toSave.owner = data.owner
    updateModel(data)
  }
}
