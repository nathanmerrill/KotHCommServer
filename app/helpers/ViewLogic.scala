package helpers

import models.{Challenge, Tournament}
import play.api.mvc.RequestHeader


object ViewLogic {
  def isActive(tournament: Tournament): Boolean =
    tournament.challenge.activeTournament != null &&
      tournament.challenge.activeTournament.id == tournament.id &&
      tournament.challenge.status == Challenge.Status.Active

  def canEdit(challenge: Challenge)(implicit request: RequestHeader): Boolean =
    challenge.owner.id.toString == request.session.get("userid").getOrElse("") ||
      request.session.get("role").getOrElse("") == "ADMIN"
}
