package controllers

import javax.inject._
import models.{Challenge, User}
import play.api.Configuration
import play.api.data.{Form, _}
import play.api.mvc._
import repository.{ChallengeRepository, UserRepository}
import play.api.data.Forms._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import helpers.Enum

@Singleton
class ChallengeController @Inject()(val cc: ControllerComponents, challenges: ChallengeRepository, users: UserRepository, controllers: Controllers, implicit val config: Configuration)
  extends AbstractController(cc) with play.api.i18n.I18nSupport {

  def index: Action[AnyContent] = Action.async { implicit request =>
    challenges.all().map(list => {
      Ok(views.html.list(list))
    })
  }

  /**
    * Display the 'edit form' of an existing Challenge
    *
    * @param id Id of the computer to edit
    */
  def edit(id: Long): Action[AnyContent] = Action.async { implicit request =>
    challenges.view(id).map {
      case None => Results.NotFound
      case Some(c) => Ok(views.html.challenge.edit(id, challengeForm.fill(c)))
    }
  }

  def create(): Action[AnyContent] = Action.async { implicit request =>
    controllers.userCheck(User.UserRole.CREATOR, _ =>
      Future.successful(Ok(views.html.challenge.create(challengeForm)))
    )
  }

  def save(): Action[AnyContent] = Action.async { implicit request =>
    controllers.userCheck(User.UserRole.CREATOR, user =>
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
    controllers.challengeCheck(id, (user, challenge) =>
      Future.successful(challengeForm.bindFromRequest.fold(
        formWithErrors => {
          BadRequest(views.html.challenge.edit(id, formWithErrors))
        },
        challengeInfo => {
          challengeInfo.id = id
          if (user.role != User.UserRole.ADMIN){
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

  def checkChallenge(id: Long): Action[AnyContent] = Action.async { implicit request =>

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
