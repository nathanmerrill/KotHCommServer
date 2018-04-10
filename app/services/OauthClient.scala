package services

import java.net.URI

import akka.actor.ActorSystem
import akka.http.scaladsl.model.Uri
import akka.stream.{ActorMaterializer, Materializer}
import com.github.dakatsuka.akka.http.oauth2.client.{AccessToken, Client, Config, GrantType}
import com.github.dakatsuka.akka.http.oauth2.client.Error.UnauthorizedException
import com.github.dakatsuka.akka.http.oauth2.client.strategy._
import javax.inject.Inject
import play.api.Application

import scala.concurrent.{ExecutionContext, Future}

class OauthClient @Inject()(app: Application) {

  implicit val system: ActorSystem = ActorSystem()
  implicit val ec: ExecutionContext = system.dispatcher
  implicit val mat: Materializer = ActorMaterializer()

  val config = Config(
    clientId = app.configuration.get("oauth.stackexchange.clientId"),
    clientSecret = app.configuration.get("oauth.stackexchange.secret"),
    site = URI.create("https://stackoverflow.com")
  )

  val client: Client = new Client(config)

  // Some(https://api.example.com/oauth/authorize?redirect_uri=https://example.com/oauth2/callback&response_type=code&client_id=xxxxxxxxx)
  val authorizeUrl: Option[Uri] =
    client.getAuthorizeUrl(GrantType.AuthorizationCode, Map("redirect_uri" -> "https://nmerrill.com/oauth2/callback", "scope" -> "write_access"))

  val accessToken: Future[Either[Throwable, AccessToken]] =
    client.getAccessToken(GrantType.AuthorizationCode, Map("code" -> "yyyyyy", "redirect_uri" -> "https://example.com"))

  accessToken.  foreach {
    case Right(t) =>
      t.accessToken // String
      t.tokenType // String
      t.expiresIn // Int
      t.refreshToken // Option[String]
    case Left(ex: UnauthorizedException) =>
      ex.code // Code
      ex.description // String
      ex.response // HttpResponse
  }

  val newAccessToken: Future[Either[Throwable, AccessToken]] =
    client.getAccessToken(GrantType.RefreshToken, Map("refresh_token" -> "zzzzzzzz"))
}
