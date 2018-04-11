package controllers


import io.ebean.Ebean
import javax.inject._
import play.api.libs.json.{JsError, JsSuccess, JsValue, Json}
import play.api.{Configuration, Logger}
import play.api.libs.ws.WSClient
import play.api.mvc._
import repository.UserRepository

import scala.concurrent._
import ExecutionContext.Implicits.global

@Singleton
class HomeController @Inject()(val cc: ControllerComponents, ws: WSClient, config: Configuration, users: UserRepository) extends AbstractController(cc) with play.api.i18n.I18nSupport {


  def auth(): Action[AnyContent] = Action.async { implicit request =>
    val codeOpt = request.getQueryString("code")
    val stateOpt = request.getQueryString("state")
    if (codeOpt.isEmpty || stateOpt.isEmpty){
      Future.successful(NoContent)
    } else {
      val code = codeOpt.get
      val state = stateOpt.get
      val key = config.get[String]("oauth.stackexchange.key")
      oauth(code).flatMap(accessToken => {
        userDetails(key, accessToken)
          .map(seUser => (seUser, accessToken))
      }).flatMap(tuple => {
        val (seUser, accessToken) = tuple
        users.insertOrUpdate(seUser.userId, seUser.name, accessToken)
      }).map(user => {
        Redirect(state).withSession(request.session + ("user" -> Ebean.json().toJson(user)) + ("name" -> user.name) + ("username" -> user.username))
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

  private def oauth(code: String)(implicit request: Request[AnyContent]): Future[String] = {
    ws.url("https://stackoverflow.com/oauth/access_token/json")
      .post(Map(
        "client_id" -> Seq(config.get[String]("oauth.stackexchange.clientId")),
        "client_secret" -> Seq(config.get[String]("oauth.stackexchange.secret")),
        "code" -> Seq(code),
        "redirect_uri" -> Seq(routes.HomeController.auth().absoluteURL())
      )).map(response => {
      val json: JsValue = Json.parse(response.body)
      val errorOpt = (json \ "error_message").asOpt[String]
      if (errorOpt.isDefined){
        throw UnableToProcessRequest("Error when authenticating: "+errorOpt.get)
      }
      (json \ "access_token").validate[String] match {
        case _: JsError =>
          Logger.error("Unable to authenticate with StackExchange.  Response: " + response.body)
          throw UnableToProcessRequest("Unable to authenticate with StackExchange")
        case s: JsSuccess[String] => s.get
      }
    })
  }

  private def userDetails(key: String, accessToken: String): Future[SEUser] = {
    ws.url("https://api.stackexchange.com/2.2/me?site=codegolf&filter=!*MxJcsxUh11DqknL&key=" + key + "&access_token=" + accessToken)
      .get()
      .map(response => {
        val json: JsValue = Json.parse(response.body)
        val errorOpt = (json \ "error_message").asOpt[String]
        if (errorOpt.isDefined){
          throw new Exception(errorOpt.get)
        }
        val user: JsValue = (json \ "items" \ 0).get
        val name: String = (user \ "display_name").as[String]
        val userId: Int = (user \ "user_id").as[Int]
        SEUser(name, userId.toString)
      })
  }
}

case class SEUser(name: String, userId: String)

case class UnableToProcessRequest(reason: String) extends Exception(reason)
