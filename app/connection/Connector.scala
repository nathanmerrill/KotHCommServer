package connection

import sys.process._

case class Status(success: Boolean, out: String, err: String)

//TODO:  akka Actors

class Connector(connectionAdapter: ConnectionAdapter) {
  def build(): Status = {
    exec(connectionAdapter.build())
  }

  def buildSubmission(): Status = {
    exec(connectionAdapter.runCommand("buildSubmission"))  //TODO:  provide name + language + code
  }

  def runGame(): Status = {
    exec(connectionAdapter.runCommand("runGame"))  //TODO:  provide player list, random
  }

  private def exec(command: String): Status ={
    val errbuffer = new StringBuffer
    val outbuffer = new StringBuffer
    val status = command ! ProcessLogger(outbuffer append _, errbuffer append _)
    Status(status == 0, outbuffer.toString, errbuffer.toString)
  }

}
