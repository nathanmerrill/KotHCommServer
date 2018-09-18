package controllers

import comm.{Downloader, FutureRunner, UnableToDownloadException}
import scala.collection.JavaConverters._
import helpers.Enum
import javax.inject._
import models.{Challenge, User}
import play.api.Configuration
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.Json
import play.api.mvc._

import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class ChallengeController @Inject()(val cc: ControllerComponents, downloader: Downloader, futureRunner: FutureRunner, implicit val config: Configuration) extends KothController(cc) {

  def index: Action[AnyContent] = Action.async { implicit request =>
    challenges.all().map(list => {
      Ok(views.html.list(list))
    })
  }

  def edit(id: Long): Action[AnyContent] = Action.async { implicit request =>
    challenges.view(id).map {
      case None => Results.NotFound
      case Some(c) => Ok(views.html.challenge.edit(id, challengeForm.fill(c)))
    }
  }

  def create(): Action[AnyContent] = Action.async { implicit request =>
    userCheck(User.UserRole.CREATOR, _ =>
      Future.successful(Ok(views.html.challenge.create(challengeForm)))
    )
  }

  def save(): Action[AnyContent] = Action.async { implicit request =>
    userCheck(User.UserRole.CREATOR, user =>
      challengeForm.bindFromRequest.fold(
        formWithErrors => {
          Future.successful(BadRequest(views.html.challenge.create(formWithErrors)))
        },
        challenge => {
          challenge.owner = user
          challenge.status = Challenge.Status.Pending
          challenges.insert(challenge).map(challenge =>
            Redirect(routes.ChallengeController.view(challenge.id))
          )
        }
      )
    )
  }

  def update(id: Long): Action[AnyContent] = Action.async { implicit request =>
    challengeCheck(id, (user, challenge) =>
      Future.successful(challengeForm.bindFromRequest.fold(
        formWithErrors => {
          BadRequest(views.html.challenge.edit(id, formWithErrors))
        },
        challengeInfo => {
          challengeInfo.id = id
          if (user.role != User.UserRole.ADMIN) {
            challengeInfo.status = challenge.status
          }
          challenges.update(challengeInfo)
          Redirect(routes.ChallengeController.view(id))
        }
      ))
    )
  }

  def view(id: Long): Action[AnyContent] = Action.async { implicit request =>
    challenges.view(id).map {
      case None => Results.NotFound
      case Some(challenge) => Ok(views.html.challenge.view(challenge))
    }
  }

  def readyChallenge(id: Long): Action[AnyContent] = Action.async { implicit request =>
    challenges.view(id).flatMap {
      case None => Future.successful(Results.NotFound)
      case Some(challenge) =>
        val errors = ListBuffer[String]()
        var repoValid = true
        var challengeValid = true
        var futures = List[Future[Any]]()
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
          challengeValid = false
        } else {
          val future = downloader.downloadSubmissions(challenge.refId)
            .recover {
            case e: UnableToDownloadException =>
              errors += "Error when attempting to read challenge: " + e.getMessage
              challengeValid = false
              Seq()
          }.flatMap { entries =>
            challenge.entries = entries.toList.asJava
            challenges.insert(challenge)
          }
          futures += future
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

  val challengeForm: Form[Challenge] = Form[Challenge](
    mapping(
      "name" -> nonEmptyText,
      "repoUrl" -> text,
      "refId" -> optional(number),
      "language" -> Enum.enumContains(classOf[Challenge.Language]),
      "buildParameters" -> text,
      "status" -> optional(Enum.enumContains(classOf[Challenge.Status]))
    )((name: String, repoUrl: String, refId: Option[Int], language: Challenge.Language, buildParameters: String, status: Option[Challenge.Status]) => {
      val newChallenge = new Challenge
      newChallenge.name = name
      newChallenge.repoUrl = repoUrl
      newChallenge.refId = refId.map(i => i.toString).getOrElse("")
      newChallenge.language = language
      newChallenge.buildParameters = buildParameters
      if (status.isDefined) {
        newChallenge.status = status.get
      }
      newChallenge
    })(challenge => {
      Some((challenge.name, challenge.repoUrl, Some(Integer.parseInt(challenge.refId)), challenge.language, challenge.buildParameters, Some(challenge.status)))
    })
  )
}
