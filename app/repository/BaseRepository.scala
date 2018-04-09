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

  private def ebeanServer: EbeanServer = Ebean.getServer(ebeanConfig.defaultServer)

  protected def query: Query[T] = ebeanServer.find(modelClass)

  protected def execute[U](query: => U): Future[U] = Future[U] {
    query
  }(executionContext)

  protected def getList[U](query: => Query[T]): Future[List[T]] =
    execute {
      query.findList().asScala.toList
    }

  protected def getOne(query: => Query[T]): Long => Future[Option[T]] =
    (id) => execute {
      Option(query.setId(id).findOne())
    }

  def getOne(id: Long): Future[Option[T]] = getOne {
    this.query
  }(id)

  protected def updateModel(data: T): Future[Option[T]] = Future {
    val transaction = ebeanServer.beginTransaction
    try {
      val saved = query.setId(data.id).findOne()
      if (saved != null) {
        saved.update()
        transaction.commit()
      }
      Option(saved)
    } finally transaction.end()
  }(executionContext)


  def insert(data: T): Future[T] = Future {
    data.id = null
    ebeanServer.insert(data)
    data
  }(executionContext)
}
