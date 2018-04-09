package controllers

import javax.inject._
import models.Challenge
import play.api.mvc._
import repository.ChallengeRepository

import scala.concurrent.ExecutionContext.Implicits.global
import play.data.FormFactory

@Singleton
class ChallengeController @Inject()(val controllerComponents: ControllerComponents, challenges: ChallengeRepository, formFactory: FormFactory) extends BaseController {

  def index: Action[AnyContent] = Action.async {
    challenges.all().map(list => {

      Ok(views.html.list.render(list))
    })
  }

  /**
    * Display the 'edit form' of a existing Computer.
    *
    * @param id Id of the computer to edit
    */
  def edit(id: Long): Action[AnyContent] = Action.async{
    challenges.view(id).map {
      case None => Results.NotFound
      case Some(c) => Ok(views.html.editForm.render(id, formFactory.form(classOf[Challenge]).fill(c)))
    }
  }

  def view(id: Long): Action[AnyContent] = Action.async{
    challenges.view(id).map(_ => Ok("test"))
  }


}
