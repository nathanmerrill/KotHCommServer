package comm

import java.io.File

import sys.process._

import scala.sys.process.ProcessLogger

object CmdTasks {
  def LatestControllerVersion(gitUrl: String): Either[ExecutionStatus, String] = {
    val command: Seq[String] =
      Seq("git", "ls-remote", gitUrl, "HEAD")
    val result = execute(command, new File("."))
    if (result.success) {
      Right(result.stdout)
    } else {
      Left(result)
    }
  }


  def UpdateController(gitUrl: String, hash: String, cwd: File): List[ExecutionStatus] = {
    val commands: List[Seq[String]] = List(
      Seq("git", "rm", "-rf", "*"),
      Seq("rm", ".git", "-rf"),
      Seq("git", "init"),
      Seq("git", "remote", "add", "origin", gitUrl),
      Seq("git", "fetch", "origin"),
      Seq("git", "checkout", hash)
    )
    commands.map { command =>
      execute(command, cwd)
    }

  }

  def BuildController(adapter: ConnectionAdapter, cwd: File): List[ExecutionStatus] = {
    val commands = adapter.build()
    commands.map(command => execute(command, cwd))
  }


  def BuildEntry(adapter: ConnectionAdapter, entry: Entry, cwd: File): ExecutionStatus = {
    val command = adapter.runCommand(Seq("build", entry.name, entry.code))
    execute(command, cwd)
  }

  def RunGame(adapter: ConnectionAdapter, seed: Long, entries: Seq[Entry], cwd: File): ExecutionStatus = {

    val command = adapter.runCommand(Seq("run", seed.toString) ++ entries.map(e => e.name))
    execute(command, cwd)
  }

  private def execute(command: Seq[String], cwd: File): ExecutionStatus = {
    val errBuffer = new StringBuffer
    val outBuffer = new StringBuffer
    val status = command ! ProcessLogger(outBuffer append _, errBuffer append _)
    ExecutionStatus(status == 0, outBuffer.toString, errBuffer.toString, command.mkString(" "))
  }


  case class ExecutionStatus(success: Boolean, stdout: String, stderr: String, command: String)

}
