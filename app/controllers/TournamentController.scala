package controllers

import comm.{Downloader, UnableToDownloadException}
import helpers.Enum
import javax.inject._
import models.{Challenge, Group, Tournament}
import play.api.Configuration
import play.api.data.{Form, Mapping}
import play.api.data.Forms._
import play.api.libs.json.Json
import play.api.mvc._
import scala.collection.JavaConverters._

import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Random

@Singleton
class TournamentController @Inject()(val cc: ControllerComponents, downloader: Downloader, implicit val config: Configuration) extends KothController(cc) {

  def edit(id: Long): Action[AnyContent] = Action.async { implicit request =>
    tournaments.view(id).map {
      case None => Results.NotFound
      case Some(c) => Ok(views.html.tournament.edit(c, tournamentForm.fill(c)))
    }
  }

  def create(challengeId: Long): Action[AnyContent] = Action.async { implicit request =>
    challengeCheck(challengeId, (_, _) =>
      Future.successful(Ok(views.html.tournament.create(challengeId, tournamentForm)))
    )
  }

  def save(challengeId: Long): Action[AnyContent] = Action.async { implicit request =>
    challengeCheck(challengeId, (_, _) =>
      tournamentForm.bindFromRequest.fold(
        formWithErrors => {
          Future.successful(BadRequest(views.html.tournament.create(challengeId, formWithErrors)))
        },
        tournament => {
          tournaments.count(challengeId).flatMap(count => {
            tournament.configuration = count + 1
            tournaments.insert(tournament).map(tournament =>
              Redirect(routes.TournamentController.view(tournament.id))
            )
          })
        }
      )
    )
  }

  def update(id: Long): Action[AnyContent] = Action.async { implicit request =>
    tournaments.view(id).flatMap {
      case None => Future.successful(Results.NotFound)
      case Some(tournament) =>
        challengeCheck(tournament.challenge.id, (_, _) =>
          Future.successful(tournamentForm.bindFromRequest.fold(
            formWithErrors => {
              BadRequest(views.html.tournament.edit(tournament, formWithErrors))
            },
            tournamentInfo => {
              tournaments.update(tournamentInfo)
              Redirect(routes.TournamentController.view(id))
            }
          ))
        )
    }
  }

  def view(id: Long): Action[AnyContent] = Action.async { implicit request =>
    tournaments.view(id).map {
      case None => Results.NotFound
      case Some(tournament) => Ok(views.html.tournament.view(tournament))
    }
  }

  def checkChallenge(id: Long): Action[AnyContent] = Action.async { implicit request =>
    challenges.view(id).flatMap {
      case None => Future.successful(Results.NotFound)
      case Some(challenge) =>
        val errors = ListBuffer[String]()
        var repoValid = true
        var challengeValid = true
        val futures = ListBuffer[Future[Any]]()
        if (challenge.repoUrl.isEmpty) {
          errors += "Unable to check controller: Please add a git URL"
          repoValid = false
        }
        if (challenge.buildParameters.isEmpty && this.requiresBuildParameters(challenge.language)) {
          errors += (challenge.language.toString + " requires Process name")
          repoValid = false
        }
        if (challenge.refId.isEmpty) {
          errors += "Unable to check Stack Exchange post.  Please add the ID of the challenge post"
          Future.successful(Results.NotFound)
          challengeValid = false
        } else {
          futures += downloader.downloadQuestions(challenge.refId).recover {
            case e: UnableToDownloadException =>
              errors + "Error when attempting to read challenge: " + e.getMessage
              challengeValid = false
              Seq()
          }
        }
        if (repoValid) {

        }
        var response: Future[Any] = Future.successful("")
        futures.foreach { future =>
          response = response.flatMap(_ => future)
        }
        response.map(_ =>
          Ok(Json.stringify(Json.obj(
            "errors" -> Json.arr(errors),
            "challengeValid" -> challengeValid.toString,
            "repoValid" -> repoValid.toString
          ))))
    }
  }

  // Check stack exchange ID
  // Check if git repo exists
  // Attempt compiling controller
  // Attempt building a submission
  // Attempt running a game

  def requiresBuildParameters(language: Challenge.Language): Boolean = {
    Set(Challenge.Language.JAVA, Challenge.Language.PYTHON_2, Challenge.Language.PYTHON_3).contains(language)
  }

  private val groupMapping: Mapping[Group] = mapping(
    "name" -> text,
    "size" -> number,
    "matchmaker" -> Enum.enumContains(classOf[Group.Matchmaker]),
    "matchmakerParameters" -> text,
    "scorer" -> Enum.enumContains(classOf[Group.Scorer]),
    "rankDescending" -> boolean,
  )((name: String, size: Int, matchmaker: Group.Matchmaker, matchmakerParameters: String, scorer: Group.Scorer, rankDescending: Boolean) => {
    val newGroup = new Group
    newGroup.name = name
    newGroup.size = size
    newGroup.matchmaker = matchmaker
    newGroup.matchmakerParameters = matchmakerParameters
    newGroup.scorer = scorer
    newGroup.rankDescending = rankDescending
    newGroup
  })(group => {
    Some((group.name, group.size, group.matchmaker, group.matchmakerParameters, group.scorer, group.rankDescending))
  })

  private val tournamentForm: Form[Tournament] = Form[Tournament](
    mapping(
      "gitHash" -> text,
      "iterationGoal" -> number,
      "group" -> list[Group](groupMapping),
    )((gitHash: String, iterationGoal: Int, groups: List[Group]) => {
      val newTournament = new Tournament
      newTournament.gitHash = gitHash
      newTournament.iterationGoal = iterationGoal
      newTournament.version = Random.alphanumeric.take(12).toString()
      newTournament.groups = groups.asJava
      newTournament
    })(tournament => {
      Some((tournament.gitHash, tournament.iterationGoal, tournament.groups.asScala.toList))
    })
  )
}
