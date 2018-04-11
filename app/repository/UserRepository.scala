package repository

import javax.inject._
import models.User
import play.db.ebean.EbeanConfig

import scala.concurrent.Future

class UserRepository @Inject()(val ebeanConfig: EbeanConfig, val executionContext: DatabaseExecutionContext) extends BaseRepository[User] {
  val modelClass: Class[User] = classOf[User]


  def view(id: Long): Future[Option[User]] =
    getOne {
      query
        .select("id,username,name")
        .fetch("entries", "id,currentName")
        .fetch("entries.User", "name")
        .fetch("Users", "id,name")
    }(id)

  def byUsername(username: String): Future[Option[User]] =
    execute {
      fetchByUsername(username)
    }

  def insertOrUpdate(username: String, name: String, authentication: String): Future[User] = {
    execute {
      val user = fetchByUsername(username) match {
        case Some(u) => u
        case None =>
          val u = new User
          u.username = username
          u.role = User.UserRole.STANDARD
          u
      }
      user.name = name
      if (user.role == User.UserRole.STANDARD) {
        user.authentication = authentication
      }
      ebeanServer.save(user)
      user
    }
  }

  private def fetchByUsername(username: String): Option[User] = {
    Option(query
      .select("id,username,name,authentication,role")
      .where().eq("username", username)
      .findOne())
  }

}
