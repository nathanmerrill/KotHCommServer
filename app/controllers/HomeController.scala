package controllers


import javax.inject._
import play.api.mvc._
import repository.ChallengeRepository
import scala.concurrent._
import ExecutionContext.Implicits.global

@Singleton
class HomeController @Inject()(val cc: ControllerComponents, challenges: ChallengeRepository) extends AbstractController(cc) with play.api.i18n.I18nSupport {

  def index: Action[AnyContent] = Action.async { implicit request =>
    challenges.all().map(list => {
      Ok(views.html.list.render(list))
    })
  }

}
