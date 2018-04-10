package controllers


import javax.inject._
import models.User
import play.api.libs.json.{JsValue, Json}
import play.api.Configuration
import play.api.libs.ws.{WSClient, WSResponse}
import play.api.mvc._
import repository.UserRepository

import scala.concurrent._
import ExecutionContext.Implicits.global

@Singleton
class HomeController @Inject()(val cc: ControllerComponents, ws: WSClient, config: Configuration, users: UserRepository) extends AbstractController(cc) with play.api.i18n.I18nSupport {


  def auth(code: String = "", state: String = ""): Action[AnyContent] = Action.async { implicit request =>
    if (code == null || code == "" || state == null || state == "") {
      Future.successful(NoContent)
    } else {
      val key = config.get[String]("oauth.stackexchange.key")
      oauth(code).flatMap(accessToken => {
        userDetails(key, accessToken)
          .map(seUser => (seUser, accessToken))
      }).flatMap(tuple => {
        val (seUser, accessToken) = tuple
        users.insertOrUpdate(seUser.userId, seUser.name, accessToken)
      }).map(user => {
        Redirect(state).withSession(request.session + ("username" -> user.username)+("name" -> user.name)+("userid" -> user.id.toString))
      })
    }
  }

  private def oauth(code: String): Future[String] ={
    ws.url("https://stackoverflow.com/oauth/access_token/json")
      .post(Map(
        "client_id" -> Seq(config.get[String]("oauth.stackexchange.clientId")),
        "client_secret" -> Seq(config.get[String]("oauth.stackexchange.secret")),
        "code" -> Seq(code),
        "redirect_uri" -> Seq(routes.HomeController.auth().absoluteURL())
      )).map(response => {
        val json: JsValue = Json.parse(response.body)
        (json \ "access_token").get.toString()
    })
  }

  private def userDetails(key: String, accessToken: String):Future[SEUser]={
    ws.url("https://api.stackexchange.com/2.2/me?site=codegolf&filter=!*MxJcsxUh11DqknL&key=" + key + "access_token=" + accessToken)
      .get()
      .map(response => {
        val json: JsValue = Json.parse(response.body)
        val user: JsValue = (json \ "items" \ 0).get
        val name: String = (user \ "display_name").get.toString()
        val userId: String = (user \ "user_id").get.toString()
        SEUser(name, userId)
      })
  }

  case class SEUser(name:String, userId:String)
}
