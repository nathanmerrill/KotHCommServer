package comm

import models.Challenge

object Adapters {
  def create(language: Challenge.Language, buildParameters: String): ConnectionAdapter ={
    language match {
      case Challenge.Language.GRADLE => new GradleConnectionAdapter()
      case Challenge.Language.JAVA => new JavaConnectionAdapter(buildParameters)
      case Challenge.Language.NPM => new NpmConnectionAdapter()
      case Challenge.Language.PYTHON_2 => new Py2ConnectionAdapter(buildParameters)
      case Challenge.Language.PYTHON_3 => new Py3ConnectionAdapter(buildParameters)
    }
  }
}

trait ConnectionAdapter {
  def build(): List[Seq[String]]
  def runCommand(command: Seq[String]): Seq[String]
}

class JavaConnectionAdapter(val mainClass: String) extends ConnectionAdapter {
  val javaFile: String = mainClass.replace(".", "/") + ".java"

  def build(): List[Seq[String]] = {
    List(Seq("javac","-classpath",".",javaFile))
  }

  def runCommand(command: Seq[String]): Seq[String] = {
    Seq("java", mainClass) ++ command
  }

}

class GradleConnectionAdapter() extends ConnectionAdapter {

  def build(): List[Seq[String]] = {
    List(Seq("gradle", "build"))
  }

  def runCommand(command: Seq[String]): Seq[String] = {
    Seq("gradle","run", "-PappArgs=[",command.mkString("'","','","'")+"]")
  }

}


class NpmConnectionAdapter() extends ConnectionAdapter {

  def build(): List[Seq[String]] = {
    List(
      Seq("npm","install"),
      Seq("npm", "build")
    )
  }

  def runCommand(command: Seq[String]): Seq[String] = {
    Seq("npm", "run", command.head, "--") ++ command.tail
  }

}

class Py2ConnectionAdapter(val mainFile: String) extends ConnectionAdapter {

  def build(): List[Seq[String]] = {
    List()
  }

  def runCommand(command: Seq[String]): Seq[String] = {
    Seq("python", mainFile) ++ command
  }

}


class Py3ConnectionAdapter(val mainFile: String) extends ConnectionAdapter {

  def build(): List[Seq[String]] = {
    List()
  }

  def runCommand(command: Seq[String]): Seq[String] = {
    Seq("python3", mainFile) ++ command
  }

}
