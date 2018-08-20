package comm

import java.io.File

import scala.concurrent.{ExecutionContext, Future}
import sys.process._
import scala.sys.process.ProcessLogger

object CmdTasks {
  def LatestControllerVersion(gitUrl: String)(implicit executor: ExecutionContext): Future[ExecutionStatus] = {
    val command: Seq[String] =
        Seq("git", "ls-remote", gitUrl, "HEAD")
    execute(command, new File("."))
  }


  def UpdateController(gitUrl: String, hash: String, cwd: File)(implicit executor: ExecutionContext): Future[Seq[ExecutionStatus]] = {
    val commands: List[Seq[String]] = List(
      Seq("git", "rm", "-rf", "*"),
      Seq("rm", ".git", "-rf"),
      Seq("git", "init"),
      Seq("git", "remote", "add", "origin", gitUrl),
      Seq("git", "fetch", "origin"),
      Seq("git", "checkout", hash)
    )
    executeAll(commands, cwd)
  }

  def BuildController(adapter: ConnectionAdapter, cwd: File)(implicit executor: ExecutionContext): Future[Seq[ExecutionStatus]] = {
    val commands = adapter.build()
    executeAll(commands, cwd)
  }


  def BuildEntry(adapter: ConnectionAdapter, entry: Entry, cwd: File)(implicit executor: ExecutionContext): Future[ExecutionStatus] = {
    val command = adapter.runCommand(Seq("build", entry.name, entry.code))
    execute(command, cwd)
  }

  def RunGame(adapter: ConnectionAdapter, seed: Long, entries: Seq[Entry], cwd: File)(implicit executor: ExecutionContext): Future[ExecutionStatus] = {
    val command = adapter.runCommand(Seq("run", seed.toString) ++ entries.map(e => e.name))
    execute(command, cwd)
  }

  private def executeAll(commands:Seq[Seq[String]], cwd: File)(implicit executor: ExecutionContext): Future[Seq[ExecutionStatus]] = {
    seq(commands.map(execute(_, cwd)))
  }

  private def seq[M](futures: Seq[Future[M]])(implicit executor: ExecutionContext): Future[Seq[M]] = {
    futures.foldLeft(Future.successful(Seq[M]())) {
      (futureSeq, future) => {
        futureSeq.flatMap(seq => future.map(result => result +: seq))
      }
    }
  }

  private def execute(command: Seq[String], cwd: File)(implicit executor: ExecutionContext): Future[ExecutionStatus] = {
    val errBuffer = new StringBuffer
    val outBuffer = new StringBuffer
    Future {
      val status = command ! ProcessLogger(outBuffer append _, errBuffer append _)
      val ret = ExecutionStatus(status == 0, outBuffer.toString, errBuffer.toString, command.mkString(" "))
      if (status != 0) {
        throw CmdException(ret)
      }
      ExecutionStatus(status == 0, outBuffer.toString, errBuffer.toString, command.mkString(" "))
    }
  }
}


case class ExecutionStatus(success: Boolean, stdout: String, stderr: String, command: String)


case class CmdException(executionStatus: ExecutionStatus) extends Exception(executionStatus.stderr)
