package controllers

import helpers.Enum
import javax.inject._
import models.User
import play.api.Configuration
import play.api.data.Form
import play.api.data.Forms.mapping
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class UserController @Inject()(cc: ControllerComponents, implicit val config: Configuration) extends KothController(cc){


  def me(): Action[AnyContent] = Action.async { implicit request =>
    userCheck(User.UserRole.STANDARD){ user =>
      Future.successful(Redirect(routes.UserController.view(user.id)))
    }
  }


  def view(id: Long): Action[AnyContent] = Action.async { implicit request =>
    users.view(id).map {
      case None => NoContent
      case Some(user) => Ok(views.html.user(user, userForm.fill(user)))
    }
  }

  def update(id: Long): Action[AnyContent] = Action.async { implicit request =>
    userCheck(User.UserRole.ADMIN){ _ =>
      users.getOne(id).flatMap {
        case None => redirectHome
        case Some(user) =>
          userForm.bindFromRequest.fold(
            formWithErrors => {
              Future.successful(BadRequest(views.html.user(user, formWithErrors)))
            },
            userInfo => {
              userInfo.id = id
              users.update(userInfo)
              Future.successful(Redirect(routes.ChallengeController.index()))
            }
          )
      }
    }
  }

  val userForm: Form[User] = Form[User](
    mapping(
      "role" -> Enum.enumContains(classOf[User.UserRole])
    )((role: User.UserRole) => {
      val newUser = new User
      newUser.role = role
      newUser
    })(user => {
      Some(user.role)
    })
  )

}
