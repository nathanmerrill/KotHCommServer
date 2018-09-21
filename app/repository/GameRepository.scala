package repository

import javax.inject._
import models.Game
import play.db.ebean.EbeanConfig

import scala.concurrent.Future

class GameRepository @Inject()(val ebeanConfig: EbeanConfig, val executionContext: DatabaseExecutionContext) extends BaseRepository[Game] {
  val modelClass: Class[Game] = classOf[Game]

  def view(id: Long): Future[Option[Game]] =
    getOneWhere(id) {
      query
        .fetch("tournament", "")
        .fetch("scores", "score")
        .fetch("scores.tournamentEntry.version", "name")
    }
}
