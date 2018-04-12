package connection

trait ConnectionAdapter {
  def build(): String
  def runCommand(command: String): String
}

class JavaConnectionAdapter(val mainClass: String) extends ConnectionAdapter {
  val javaFile: String = mainClass.replace(".", "/") + ".java"

  def build(): String = {
    "javac -classpath . " + javaFile
  }

  def runCommand(command: String): String = {
    "java " + mainClass + " " + command
  }

}
