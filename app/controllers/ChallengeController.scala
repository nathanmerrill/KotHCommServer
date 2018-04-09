package controllers

import io.ebean.annotation.EnumValue
import javax.inject._
import models.Challenge
import play.api.data.{Form, _}
import play.api.mvc._
import repository.ChallengeRepository
import play.api.data.Forms._
import play.api.data.format.Formatter

import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class ChallengeController @Inject()(val cc: ControllerComponents, challenges: ChallengeRepository)
  extends AbstractController(cc) with play.api.i18n.I18nSupport{

  def index: Action[AnyContent] = Action.async { implicit request =>
    challenges.all().map(list => {

      Ok(views.html.list.render(list))
    })
  }


  val challengeForm: Form[Challenge] = Form[Challenge](
      mapping(
      "name" -> nonEmptyText(minLength = 5),
      "source" -> enumContains(classOf[Challenge.Source]),
      "refId" -> text,
    )((name: String, source: Challenge.Source, refId:String) => {
        val newChallenge = new Challenge
        newChallenge.name = name
        newChallenge.source = source
        newChallenge.refId = refId
        newChallenge
      })(challenge => {
        Some((challenge.name, challenge.source, challenge.refId))
      })
  )

  def enumContains[T: Enum[T]](enumType: Class[T]): Mapping[T] = Forms.of[T](enumBinder(classOf[T]))

  def enumBinder[T: Enum[T]](enumType: Class[T]): Formatter[T] = new Formatter[T]{

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
          case p: _ => p.value()
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
      case Some(c) => Ok(views.html.editForm.render(id, challengeForm.fill(c)))
    }
  }

  def save(id: Long): Action[AnyContent] = Action.async { implicit request =>
    challenges.view(id).map {
      case None => Results.NotFound
      case Some(c) => challengeForm.bindFromRequest.fold(
        formWithErrors => {
          // binding failure, you retrieve the form containing errors:
          BadRequest(views.html.editForm(id, formWithErrors))
        },
        challenge => {
          challenge.owner = null //Todo: Get current user
          challenges.update(challenge)
          Redirect(routes.ChallengeController.view(id))
        }
      )
    }
  }

  def view(id: Long): Action[AnyContent] = Action.async { implicit request =>
    challenges.view(id).map(_ => Ok("test"))
  }

}
