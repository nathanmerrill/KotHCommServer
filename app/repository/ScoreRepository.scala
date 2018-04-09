package repository

import javax.inject._
import models.Score
import play.db.ebean.EbeanConfig

import scala.concurrent.Future

class ScoreRepository @Inject()(val ebeanConfig: EbeanConfig, val executionContext: DatabaseExecutionContext) extends BaseRepository[Score] {
  val modelClass: Class[Score] = classOf[Score]
}
