package controllers


import comm.SeApi
import io.ebean.Ebean
import javax.inject._
import play.api.Logger
import play.api.libs.json.JsValue
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._

@Singleton
class HomeController @Inject()(val cc: ControllerComponents, se: SeApi) extends KothController(cc) {


  def auth(): Action[AnyContent] = Action.async { implicit request =>
    val codeOpt = request.getQueryString("code")
    val stateOpt = request.getQueryString("state")
    if (codeOpt.isEmpty || stateOpt.isEmpty){
      Future.successful(NoContent)
    } else {
      val code = codeOpt.get
      val state = stateOpt.get
      se.oauth(code).flatMap {
        case Left(error) => Future.failed(UnableToProcessRequest(error))
        case Right(accessToken) =>
          userDetails(accessToken)
            .map(seUser => (seUser, accessToken))
      }.flatMap(tuple => {
        val (seUser, accessToken) = tuple
        users.insertOrUpdate(seUser.userId, seUser.name, accessToken)
      }).map(user => {
        Redirect(state).addingToSession(
          ("user", Ebean.json().toJson(user)),
          ("name", user.name),
          ("userid", user.id.toString),
          ("role", user.role.name())
        )
      }) recover { case cause =>
        cause match {
          case UnableToProcessRequest(message) => InternalServerError(message)
          case a =>
            Logger.error("Unable to process request", a)
            InternalServerError("Unable to process request")
        }
      }
    }
  }

  def deauth(): Action[AnyContent] = Action.async { implicit request =>
    Future.successful(Redirect(routes.ChallengeController.index()).withNewSession)
  }


  private def userDetails(accessToken: String): Future[SEUser] = {
    se.request("me", se.defaultParameters ++ Map(("filter", "!*MxJcsxUh11DqknL"), ("access_token", accessToken)))
      .map {
        case Left(error) => throw new Exception(error)
        case Right(json) =>
          val user: JsValue = (json \ "items" \ 0).get
          val name: String = (user \ "display_name").as[String]
          val userId: Int = (user \ "user_id").as[Int]
          SEUser(name, userId.toString)
      }
  }
}

case class SEUser(name: String, userId: String)

case class UnableToProcessRequest(reason: String) extends Exception(reason)
