package comm

import controllers.routes
import javax.inject.Inject
import play.api.libs.json.{JsError, JsSuccess, JsValue, Json}
import play.api.libs.ws.{WSClient, WSResponse}
import play.api.mvc.{AnyContent, Request}
import play.api.{Configuration, Logger}

import scala.concurrent.{ExecutionContext, Future}

class SeApi @Inject()(ws: WSClient, config: Configuration) {
  val key: String = config.get[String]("oauth.stackexchange.key")
  val defaultParameters: Map[String, String] = Map(("site", "codegolf"), ("key", key))

  def request(request: String, parameters: Map[String, String] = Map())(implicit executor: ExecutionContext) : Future[Either[String,JsValue]] = {
    val paramsString = (defaultParameters ++ parameters).map(pair => pair._1+"="+pair._2).mkString("&")
    ws.url("https://api.stackexchange.com/2.2/"+request+"?"+paramsString)
      .get()
      .map(response => {
        val json: JsValue = Json.parse(response.body)
        val errorOpt = (json \ "error_message").asOpt[String]
        if (errorOpt.isDefined) {
          Left(errorOpt.get)
        } else {
          Right(json)
        }
      })
  }


  private def parse(response: WSResponse): Either[String, JsValue] = {
      val json: JsValue = Json.parse(response.body)
      val errorOpt = (json \ "error_message").asOpt[String]
      if (errorOpt.isDefined) {
        Left(errorOpt.get)
      } else {
        Right(json)
      }
  }


  def oauth(code: String)(implicit request: Request[AnyContent], executor: ExecutionContext) : Future[Either[String, String]] = {
    ws.url("https://stackoverflow.com/oauth/access_token/json")
      .post(Map(
        "client_id" -> Seq(config.get[String]("oauth.stackexchange.clientId")),
        "client_secret" -> Seq(config.get[String]("oauth.stackexchange.secret")),
        "code" -> Seq(code),
        "redirect_uri" -> Seq(routes.HomeController.auth().absoluteURL())
      )).map(response => {
      parse(response) match {
        case Left(error) => Left("Error when authenticating: "+error)
        case Right(json) =>
          (json \ "access_token").validate[String] match {
            case _: JsError =>
              Logger.error("Unable to authenticate with StackExchange.  Response: " + response.body)
              Left("Unable to authenticate with StackExchange")
            case s: JsSuccess[String] => Right(s.get)
          }
      }
    })
  }
}
