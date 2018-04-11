package controllers


import javax.inject._
import play.api.libs.json.{JsValue, Json}
import play.api.{Configuration, Logger}
import play.api.libs.ws.{WSClient}
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
        Redirect(state).withSession(request.session + ("username" -> user.username) + ("name" -> user.name) + ("userid" -> user.id.toString))
      }) recover { case cause =>
        cause match {
          case UnableToProcessRequest(message) => InternalServerError(message)
          case _ => InternalServerError("Unable to process request")
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
      val error = json \ "error_message"
      if (error.isDefined){
        throw UnableToProcessRequest("Error when authenticating: "+error.get.toString())
      }
      val accessToken = json \ "access_token"
      if (accessToken.isEmpty) {
        Logger.error("Unable to authenticate with StackExchange.  Response: " + response.body)
        throw UnableToProcessRequest("Unable to authenticate with StackExchange")
      }
      accessToken.get.toString()
    })
  }

  private def userDetails(key: String, accessToken: String): Future[SEUser] = {
    ws.url("https://api.stackexchange.com/2.2/me?site=codegolf&filter=!*MxJcsxUh11DqknL&key=" + key + "access_token=" + accessToken)
      .get()
      .map(response => {
        val json: JsValue = Json.parse(response.body)
        val error = json \ "error_message"
        if (error.isDefined){
          throw new Exception(error.get.toString())
        }
        val user: JsValue = (json \ "items" \ 0).get
        val name: String = (user \ "display_name").get.toString()
        val userId: String = (user \ "user_id").get.toString()
        SEUser(name, userId)
      })
  }
}

case class SEUser(name: String, userId: String)

case class UnableToProcessRequest(reason: String) extends Exception(reason)
