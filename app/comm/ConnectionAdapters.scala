package comm

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
    val args:Iterable[String] = command.map(f => "'"+f+"'")
    Seq("gradle","run", "-PappArgs=[",command.mkString("'",",","'")+"]")
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
