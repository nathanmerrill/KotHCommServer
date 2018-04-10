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

  def insertOrUpdate(username: String, name: String, authentication: String): Future[User] ={
    execute {
      fetchByUsername(username) match {
        case Some(user) =>
          user.name = name
          user.authentication = authentication
          user.update()
          user
        case None =>
          val user = new User
          user.name = username
          user.authentication = authentication
          user.username = username
          user.role = User.UserRole.STANDARD
          user
      }
    }
  }

  private def fetchByUsername(username: String): Option[User] = {
    Option(query
      .select("id,username,name,authentication,role")
      .where().eq("username", username)
      .findOne())
  }

}
