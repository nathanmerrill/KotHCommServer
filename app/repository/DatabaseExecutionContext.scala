package repository

import akka.actor.ActorSystem
import play.libs.concurrent.CustomExecutionContext
import javax.inject.Inject

import scala.concurrent.ExecutionContext

/**
  * Custom execution context, so that blocking database operations don't
  * happen on the rendering thread pool.
  *
  */

class DatabaseExecutionContext @Inject()(val actorSystem: ActorSystem)
  extends CustomExecutionContext(actorSystem, "database.dispatcher") with ExecutionContext
