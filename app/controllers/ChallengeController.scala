package controllers

import io.ebean.Ebean
import io.ebean.annotation.EnumValue
import javax.inject._
import models.{Challenge, User}
import play.api.Configuration
import play.api.data.{Form, _}
import play.api.mvc._
import repository.{ChallengeRepository, UserRepository}
import play.api.data.Forms._
import play.api.data.format.Formatter

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class ChallengeController @Inject()(val cc: ControllerComponents, challenges: ChallengeRepository, users: UserRepository, implicit val config: Configuration)
  extends AbstractController(cc) with play.api.i18n.I18nSupport {

  def index: Action[AnyContent] = Action.async { implicit request =>
    challenges.all().map(list => {
      Ok(views.html.list(list))
    })
  }


  val challengeForm: Form[Challenge] = Form[Challenge](
    mapping(
      "repoUrl" -> nonEmptyText(minLength = 10),
      "refId" -> text,
    )((repoUrl: String, refId: String) => {
      val newChallenge = new Challenge
      newChallenge.refId = refId
      newChallenge.repoUrl = repoUrl
      newChallenge
    })(challenge => {
      Some((challenge.repoUrl, challenge.refId))
    })
  )

  //TODO: keeping this here because it's useful code I'll use later
  def enumContains[T <: Enum[T]](enumType: Class[T]): Mapping[T] = Forms.of[T](enumBinder(enumType))

  def enumBinder[T <: Enum[T]](enumType: Class[T]): Formatter[T] = new Formatter[T] {

    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], T] = {
      enumType.getEnumConstants.find { c =>
        val name: String = getEnumValue(c)
        name.equals(key)
      } match {
        case Some(f) => Right(f)
        case None => Left(Seq(FormError(key, "error.invalidEnum", Nil)))
      }
    }

    override def unbind(key: String, value: T): Map[String, String] = {
      Map(key -> getEnumValue(value))
    }

    def getEnumValue(value: T): String = {
      val name = value.toString
      enumType.getField(name).getAnnotation(classOf[EnumValue]) match {
        case null => name
        case p => p.value()
      }
    }
  }

  /**
    * Display the 'edit form' of an existing Challenge
    *
    * @param id Id of the computer to edit
    */
  def edit(id: Long): Action[AnyContent] = Action.async { implicit request =>
    challenges.view(id).map {
      case None => Results.NotFound
      case Some(c) => Ok(views.html.editForm(id, challengeForm.fill(c)))
    }
  }

  def create(): Action[AnyContent] = Action.async { implicit request =>
    userCheck{ _ =>
      Future.successful(Ok(views.html.createForm(challengeForm)))
    }
  }
  var redirectHome: Future[Result] =
    Future.successful(Results.Redirect(routes.ChallengeController.index()))

  def userCheck(action: User => Future[Result], id:Long = -1)(implicit request: RequestHeader): Future[Result] ={
    request.session.get("user") match {
      case None => redirectHome
      case Some(userJson) =>
        val user = Ebean.json().toBean(classOf[User], userJson)
        users.getOne(user.id.toLong).flatMap(userOpt => {
          if (userOpt.isDefined && userOpt.get.role != User.UserRole.STANDARD){
            if (id < 0) {
              action.apply(userOpt.get)
            } else {
              challenges.getOne(id).flatMap(challenge => {
                if (challenge.isDefined && challenge.get.owner.id == user.id){
                  action.apply(userOpt.get)
                } else {
                  redirectHome
                }
              })
            }
          } else {
            redirectHome
          }
        })
    }
  }



  def update(id: Long): Action[AnyContent] = Action.async { implicit request =>
    userCheck { user =>
      challenges.view(id).map {
        case None => Results.NotFound
        case Some(c) => challengeForm.bindFromRequest.fold(
          formWithErrors => {
            // binding failure, you retrieve the form containing errors:
            BadRequest(views.html.editForm(id, formWithErrors))
          },
          challenge => {
            challenge.owner = Ebean.json().toBean(classOf[User], request.session.get("user").get)
            challenges.update(challenge)
            Redirect(routes.ChallengeController.view(id))
          }
        )
      }
    }(id)
  }

  def view(id: Long): Action[AnyContent] = Action.async { implicit request =>
    challenges.view(id).map(_ => Ok("test"))
  }

}
