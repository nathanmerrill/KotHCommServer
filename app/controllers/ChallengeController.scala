package controllers

import comm.Downloader.UnableToDownloadException
import comm.{Downloader, FutureRunner}
import helpers.Enum
import javax.inject._
import models.{Challenge, Entry, EntryVersion, User}
import play.api.Configuration
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Success, Try}

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
    userCheck(User.UserRole.CREATOR) { _ =>
      Future.successful(Ok(views.html.challenge.create(challengeForm)))
    }
  }

  def save(): Action[AnyContent] = Action.async { implicit request =>
    userCheck(User.UserRole.CREATOR) { user =>
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
    }
  }

  def update(id: Long): Action[AnyContent] = Action.async { implicit request =>
    challengeCheck(id) { (user, challenge) =>
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
    }
  }

  def view(id: Long): Action[AnyContent] = Action.async { implicit request =>
    challenges.view(id).map {
      case None => Results.NotFound
      case Some(challenge) =>
        Ok(views.html.challenge.view(challenge))
    }
  }

  def fetchEntries(id: Long): Action[AnyContent] = Action.async { implicit request =>
    this.challengeCheck(id) { (_, challenge) =>
      val future: Future[Seq[Entry]] =
        if (challenge.refId.isEmpty)
          Future.failed(new UnableToDownloadException("Unable to check Stack Exchange post. Please add the ID of the StackExchange challenge post"))
        else
          downloader.downloadSubmissions(challenge.refId).flatMap { entries =>
            Future.sequence(entries.map(addEntry(_, challenge)))
          }
      future.transform { response => {
        if (response.isFailure && !response.failed.get.isInstanceOf[UnableToDownloadException]){
          throw response.failed.get
        }
        val error = response.failed.map(_.getMessage).getOrElse("")
        Success(Ok(views.html.challenge.view(challenge, List(error))))
      }
      }
    }
  }

  private def addEntry(data: Downloader.Submission, challenge: Challenge): Future[Entry] ={
    users.insertOrUpdate(data.ownerId, data.ownerName).flatMap{ user =>
      val entry = new Entry()
      entry.owner = user
      entry.challenge = challenge
      entry.currentName = data.name
      entry.refId = data.answerId
      entries.insertOrUpdateByRef(entry.refId, entry)
    }.flatMap{ entry =>
      val version = new EntryVersion()
      version.code = data.body
      version.entry = entry
      version.name = data.name
      version.language = data.language
      version.valid = data.valid
      entryVersions.insert(version).map(_ => entry)
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
      Some((challenge.name, challenge.repoUrl, Try(Integer.parseInt(challenge.refId)).toOption, challenge.language, challenge.buildParameters, Some(challenge.status)))
    })
  )
}
