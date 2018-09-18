package comm

import javax.inject.Inject
import models.Entry
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import play.api.libs.json.JsValue
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._

import scala.concurrent.{ExecutionContext, Future}

object Downloader {

  class UnableToDownloadException(message: String) extends Exception(message)
  case class Submission(name: String, body: String, answer_id: String, question_id: String, owner_name: String, user_id: String)
}


class Downloader @Inject()(se: SeApi) {
  import Downloader._

  def downloadSubmissions(questionId: String)(implicit executor: ExecutionContext) : Future[Seq[Entry]] = {
    se.request("questions/" + questionId + "/answers", Map(("filter", filter)))
      .map {
        case Left(error: String) => throw new UnableToDownloadException(error)
        case Right(json: JsValue) => {
          val response = json.as[SeResponse]

          (json \\ "body").flatMap(s => parseHtml(s.as[String])).map{ p =>
            val (name, body) = p
            new Submission(name, body, )
          }
          Seq()
        }
      }
  }

  private def parseHtml(html: String): Option[(String, String)] = {
    val document = new JsoupBrowser().parseString(html)
    val header = (document >?> text("h1,h2,h3,h4,h5,h6")).filterNot(_.contains("Invalid"))
    val code = document >?> text("pre>code")
    header.zip(code).headOption
  }

  //
  //  private def writeSubmission(submission: Submission, directory: String):Unit = try {
  //    val writer = new FileWriter(file)
  //    writer.write(contents)
  //    writer.close()
  //  } catch {
  //    case e: IOException =>
  //      throw new Nothing("Unable to save response", e)
  //  }
  //
  //  private def saveOther(codeBlock: String, directory: File): Unit = {
  //    try Files.createDirectories(directory.toPath)
  //    catch {
  //      case e: IOException =>
  //        System.out.println("Unable to create directory " + directory.getAbsolutePath)
  //        return
  //    }
  //    val lineIndex = codeBlock.indexOf('\n')
  //    val fileName = codeBlock.substring(0, lineIndex).trim
  //    if (!fileName.contains(".") || fileName.contains(" ")) {
  //      System.out.println("Skipping code block, doesn't contain valid filename: " + fileName)
  //      return
  //    }
  //    val dest = new File(directory, fileName)
  //    val contents = codeBlock.substring(lineIndex)
  //    writeFile(dest, contents)
  //  }


  private val filter = "!.FjwPGDVPw6_*p1K.dONY5*2)WjK*"
  private case class SeResponse(has_more: Boolean, quota_max: Int, quota_remaining: Int, items: List[SeAnswer])
  private case class SeAnswer(owner: SeOwner, answer_id: Int, question_id: Int, body: String)
  private case class SeOwner(user_id: Int, display_name: String)

}
