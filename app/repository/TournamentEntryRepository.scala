package repository

import javax.inject._
import models.TournamentEntry
import play.db.ebean.EbeanConfig

import scala.concurrent.Future

class TournamentEntryRepository @Inject()(val ebeanConfig: EbeanConfig, val executionContext: DatabaseExecutionContext) extends BaseRepository[TournamentEntry] {
  val modelClass: Class[TournamentEntry] = classOf[TournamentEntry]

  def view(id: Long): Future[Option[TournamentEntry]] =
    getOne {
      query
        .fetch("version", "name,code,language")
        .fetch("version.entry.owner", "id,name")
        .fetch("tournament", "id")
        .fetch("tournament.challenge", "id,name")
        .fetch("scores", "score")
        .fetch("scores.game", "id")
    }(id)

}
