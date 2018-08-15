package jobs

import javax.inject.{Inject, Named}
import akka.actor.{ActorRef, ActorSystem}
import models.{Challenge, Tournament}
import play.api.Configuration
import repository.ChallengeRepository

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration._

class GameScheduler @Inject()(actorSystem: ActorSystem, challengeRepository: ChallengeRepository, config: Configuration)(implicit executionContext: ExecutionContext) {

  actorSystem.scheduler.schedule(
    initialDelay = 0.seconds,
    interval = 30.seconds,
  ) {
    fetchGamesToExecute().foreach{ case (tournament, count) =>
        scheduleGame(tournament, count)
    }
  }


  def scheduleGame(tournament: Tournament, count: Int): Unit ={

  }

  def fetchTournaments(): List[Tournament] = {
    val challenges = Await.result(challengeRepository.activeChallenges(), 2.seconds)
    challenges
      .filter(challenge => !challenge.versions.isEmpty)
      .map {challenge =>
        challenge.versions.get(0)
      }.filter{tournament =>
      tournament.games.size() < tournament.iterationGoal
    }
  }

  def fetchGamesToExecute(): List[(Tournament, Int)] = {
    val tournaments = fetchTournaments()
    val currentlyRunning = tournaments.map { tournament =>
      tournament.games.stream().filter(game => game.endTime == null).count().toInt
    }.sum
    val maximumGames = config.get[Int]("kothcomm.concurrentGames")
    val remainder = maximumGames - currentlyRunning
    if (remainder > 0) {
      val gamesPerTournament = (remainder + tournaments.size - 1) / tournaments.size
      tournaments.map {tournament =>
        val remainingGames = tournament.iterationGoal - tournament.games.size()
        val gamesToRun =  Math.min(remainingGames, gamesPerTournament)
        (tournament, gamesToRun)
      }
    } else {
      List()
    }
  }


  def executeGames(): Unit = {

  }

}
