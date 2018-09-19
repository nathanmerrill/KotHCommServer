package comm

import javax.inject.Inject
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL._
import play.api.libs.json.JsValue

import scala.concurrent.{ExecutionContext, Future}

object Downloader {

  class UnableToDownloadException(message: String) extends Exception(message)

  case class Submission(name: String, body: String, answerId: String, ownerName: String, ownerId: String, language: String, valid: Boolean)

}


class Downloader @Inject()(se: SeApi) {

  import Downloader._

  def downloadSubmissions(questionId: String)(implicit executor: ExecutionContext): Future[Seq[Submission]] = {
    se.request("questions/" + questionId + "/answers", Map(("filter", filter)))
      .map {
        case Left(error: String) => throw new UnableToDownloadException(error)
        case Right(json: JsValue) => {
          val response = json.as[SeResponse]
          response.items.map { item =>
            val document = new JsoupBrowser().parseString(item.body)
            val header = (document >?> text("h1,h2,h3,h4,h5,h6")).getOrElse("").trim()
            val code = (document >?> text("pre>code")).getOrElse("")
            val invalid = header.contains("Invalid") || header.isEmpty || code.isEmpty
            val parts = header.split(',').take(2)
            val language = if (parts.length == 2) parts.last else ""
            val name = parts.head
            Submission(name, code, item.answer_id.toString, item.owner.display_name, item.owner.user_id.toString, language, !invalid)
          }
        }
      }
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
