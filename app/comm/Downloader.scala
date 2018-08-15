package comm

import javax.inject.Inject
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import play.api.libs.json.JsValue
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._

import scala.concurrent.{ExecutionContext, Future}


class Downloader @Inject()(se: SeApi) {

  def downloadQuestions(questionId: String)(implicit executor: ExecutionContext) : Future[Seq[Entry]] = {
    se.request("questions/" + questionId + "answers", Map(("filter", "FcbKgRqyv4bqdqoj9fAB6fZ05P")))
      .map {
        case Left(error: String) => throw new Exception(error)
        case Right(json: JsValue) =>
          (json \\ "body")
            .map(s => parseHtml(s.as[String]))
            .collect { case Some(s) => s }
      }
  }

  private def parseHtml(html: String): Option[Entry] = {
    val document = new JsoupBrowser().parseString(html)
    document >?> text("h1,h2,h3,h4,h5,h6") match {
      case None => None
      case Some(header) =>
        if (header.contains("Invalid")) {
          None
        } else {
          val name = header.split(",")(0)
          document >?> text("pre>code") match {
            case None => None
            case Some(code) => Some(Entry(name, code))
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

}

case class Entry(name: String, code: String)
