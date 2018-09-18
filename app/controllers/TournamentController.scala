package controllers

import comm.{Downloader, UnableToDownloadException}
import helpers.Enum
import javax.inject._
import models.{Challenge, Group, Tournament}
import play.api.Configuration
import play.api.data.Forms._
import play.api.data.validation.{Invalid, ValidationError}
import play.api.data.{Form, Mapping}
import play.api.libs.json.Json
import play.api.mvc._

import scala.collection.JavaConverters._
import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Random, Try}

@Singleton
class TournamentController @Inject()(val cc: ControllerComponents, downloader: Downloader, implicit val config: Configuration) extends KothController(cc) {

  def edit(id: Long): Action[AnyContent] = Action.async { implicit request =>
    tournaments.view(id).map {
      case None => Results.NotFound
      case Some(c) => Ok(views.html.tournament.edit(c, tournamentForm.fill(c)))
    }
  }

  def create(challengeId: Long): Action[AnyContent] = Action.async { implicit request =>
    challengeCheck(challengeId, (_, challenge) =>
      Future.successful(Ok(views.html.tournament.create(challenge, tournamentForm)))
    )
  }

  def save(challengeId: Long): Action[AnyContent] = Action.async { implicit request =>
    challengeCheck(challengeId, (_, challenge) =>
      tournamentForm.bindFromRequest.fold(
        formWithErrors => {
          Future.successful(BadRequest(views.html.tournament.create(challenge, formWithErrors)))
        },
        tournament => {
          tournaments.count(challengeId).flatMap(count => {
            tournament.configuration = count + 1
            tournament.challenge = challenge
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

  def requiresBuildParameters(language: Challenge.Language): Boolean = {
    Set(Challenge.Language.JAVA, Challenge.Language.PYTHON_2, Challenge.Language.PYTHON_3).contains(language)
  }

  private def validateTournament(implicit request: Request[AnyContent]): Either[Form[Tournament], Tournament] = {
    var form = tournamentForm.bindFromRequest
    form.value match {
      case None => Left(form)
      case Some(tournament) => {
        if (tournament.groups.size == 0){
          form = form.withError("group", "At least 1 group is required")
        }
        if (tournament.groups.size > 1) {

        }

        Right(tournament)
      }
    }
  }
  private def nullOrEmpty(str: String):Boolean = str == null || str.isEmpty

  def tournamentConstraint(tournament:Tournament):Seq[(String, String)] = {
      if (tournament.groups.size == 0){
        Invalid(Seq(ValidationError("At least 1 group is required")))
      }
      val multipleGroups = tournament.groups.size > 1
      val checks = List[((Group => Boolean), (String, String))](
        ((group: Group) => nullOrEmpty(group.name) && multipleGroups) -> ("name", "Name is required"),
        ((group: Group) => group.matchmaker == Group.Matchmaker.TOURNAMENT && group.size != 2) -> ("matchmaker", "Game size must be 2"),
        ((group: Group) => {
          nullOrEmpty(group.matchmakerParameters) &&
            (group.matchmaker == Group.Matchmaker.ELITIST_SELETION || group.matchmaker == Group.Matchmaker.TOURNAMENT)
        }) -> ("matchmakerParameters", "Required"),
        ((group: Group) => {
          group.scorer == Group.Scorer.CONDORCET &&
            (nullOrEmpty(group.scorerParameters) || group.scorerParameters.split(',').exists(point => Try(Integer.parseInt(point)).isFailure))
        }) -> ("scorer-parameters-condorcet", "Required"),
        ((group: Group) =>
          group.scorer == Group.Scorer.RANK_POINTS &&
            Enum.enumValue(classOf[Group.Scorer], group.scorerParameters).isEmpty
          ) -> ("scorer-parameters-rank-points", "Required")
      )

      tournament.groups.asScala.flatMap(group => {
        checks.flatMap(tuple => {
          val (check, error) = tuple
          if (check.apply(group)) {
            Some(error)
          } else {
            None
          }
        })
      })
  }

  private val groupMapping: Mapping[Group] = mapping(
    "name" -> text,
    "size" -> number.verifying("Must be positive", _ >= 0),
    "matchmaker" -> Enum.enumContains(classOf[Group.Matchmaker]),
    "matchmakerParameters" -> text,
    "scorer" -> Enum.enumContains(classOf[Group.Scorer]),
    "scorerParametersCondorcet" -> text,
    "scorerParametersRankPoints" -> text,
    "rankDescending" -> boolean,
    // https://stackoverflow.com/questions/12100698/play-framework-2-0-validate-field-in-forms-using-other-fields
  )((name: String, size: Int, matchmaker: Group.Matchmaker, matchmakerParameters: String, scorer: Group.Scorer, parameterCondorcet: String, parameterRankPoints: String, rankDescending: Boolean) => {
    val newGroup = new Group
    newGroup.name = name
    newGroup.size = size
    newGroup.matchmaker = matchmaker
    newGroup.matchmakerParameters = matchmakerParameters
    newGroup.scorer = scorer
    if (newGroup.scorer == Group.Scorer.CONDORCET){
      newGroup.matchmakerParameters = parameterCondorcet
    }
    if (newGroup.scorer == Group.Scorer.RANK_POINTS) {
      newGroup.matchmakerParameters = parameterRankPoints
    }
    newGroup.rankDescending = rankDescending
    newGroup
  })(group => {
    Some((group.name, group.size, group.matchmaker, group.matchmakerParameters, group.scorer, group.scorerParameters, group.scorerParameters, group.rankDescending))
  })

  private val tournamentForm: Form[Tournament] = Form[Tournament](
    mapping(
      "iterationGoal" -> number,
      "group" -> list[Group](groupMapping),
    )((iterationGoal: Int, groups: List[Group]) => {
      val newTournament = new Tournament
      newTournament.iterationGoal = iterationGoal
      newTournament.version = Random.alphanumeric.take(12).toString()
      newTournament.groups = groups.asJava
      newTournament
    })(tournament => {
      Some((tournament.iterationGoal, tournament.groups.asScala.toList))
    })
  )
}
