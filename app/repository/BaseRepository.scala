package repository

import io.ebean.{Ebean, EbeanServer, Query}
import models.BaseModel
import play.db.ebean.EbeanConfig

import scala.collection.JavaConverters._
import scala.concurrent.Future

abstract class BaseRepository[T <: BaseModel] {
  protected def modelClass: Class[T]

  protected def ebeanConfig: EbeanConfig

  protected def executionContext: DatabaseExecutionContext

  protected def ebeanServer: EbeanServer = Ebean.getServer(ebeanConfig.defaultServer)

  protected def query: Query[T] = ebeanServer.find(modelClass)

  protected def execute[U](query: => U): Future[U] =
    Future[U] {
      query
    }(executionContext)

  protected def getList[U](query: => Query[T]): Future[List[T]] =
    execute {
      query.findList().asScala.toList
    }

  protected def getOneWhere(id: Long)(query: => Query[T]): Future[Option[T]] =
    execute {
      Option(query.setId(id).findOne())
    }

  protected def getOne(id: Long): Future[Option[T]] =
    execute {
      Option(query.setId(id).findOne())
    }

  def update(data: T): Future[Option[T]] =
    execute {
      ebeanServer.update(data)
    }.flatMap { _ => getOne(data.id) }


  def insert(data: T): Future[T] =
    execute {
      ebeanServer.insert(data)
      data
    }

  def fetchByRef(refId: String): Future[Option[T]] =
    execute {
      Option(query
        .select("*")
        .where().eq("refId", refId)
        .findOne())
    }

  def insertOrUpdateByRef(refId: String, data: T): Future[T] =
    fetchByRef(refId).flatMap{opt =>
      if (opt.isDefined) {
        data.id = opt.get.id
        this.update(data).map(_.get)
      } else {
        this.insert(data)
      }
    }
}
