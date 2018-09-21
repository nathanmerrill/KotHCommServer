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
    getOneWhere(id) {
      query
        .fetch("versions", "id,name,createdAt")
        .fetch("owner", "id,name")
        .fetch("versions", "id,createdAt,versionId")
    }


}
