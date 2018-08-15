package controllers

import io.ebean.Ebean
import javax.inject.Inject
import models.{Challenge, User}
import play.api.mvc.{RequestHeader, Result, Results}
import repository.{ChallengeRepository, UserRepository}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class Controllers @Inject()(challenges: ChallengeRepository, users: UserRepository) {

  var redirectHome: Future[Result] =
    Future.successful(Results.Redirect(routes.ChallengeController.index()))

  def userCheck(role: User.UserRole, action: User => Future[Result])(implicit request: RequestHeader): Future[Result] = {
        request.session.get("user") match {
      case None => redirectHome
      case Some(userJson) =>
        val userData = Ebean.json().toBean(classOf[User], userJson)
        users.getOne(userData.id).flatMap(userOpt => {
          if (userOpt.isEmpty){
            Future.successful(Results.Redirect(routes.HomeController.deauth()))
          } else {
            val user = userOpt.get
            if (user.role.compareTo(role) < 0) {
              redirectHome
            } else {
              action.apply(user)
            }
          }
        })
    }
  }

  def challengeCheck(challengeId: Long, action: (User, Challenge) => Future[Result])(implicit request: RequestHeader): Future[Result] = {
    userCheck(User.UserRole.CREATOR, user => {
      challenges.getOne(challengeId).flatMap(challengeOpt => {
        if (challengeOpt.isEmpty){
          redirectHome
        } else {
          val challenge = challengeOpt.get
          if (challenge.owner.id != user.id && user.role != User.UserRole.ADMIN) {
            redirectHome
          } else {
            action.apply(user, challenge)
          }
        }
      })
    })
  }

}
