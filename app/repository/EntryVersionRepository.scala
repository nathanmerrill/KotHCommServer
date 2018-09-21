package repository

import javax.inject._
import models.EntryVersion
import play.db.ebean.EbeanConfig

class EntryVersionRepository @Inject()(val ebeanConfig: EbeanConfig, val executionContext: DatabaseExecutionContext) extends BaseRepository[EntryVersion] {
  val modelClass: Class[EntryVersion] = classOf[EntryVersion]

  def fetchLatestVersion(entryId)

}
