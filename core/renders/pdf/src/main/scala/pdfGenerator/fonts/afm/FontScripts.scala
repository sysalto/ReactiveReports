package pdfGenerator.fonts.afm

import java.io.File
import java.nio.file.Paths

import akka.stream.scaladsl.{FileIO, Framing, Sink}
import akka.util.ByteString
import com.typesafe.config.ConfigFactory

import scala.concurrent.Await

/**
  * Created by marian on 11/25/16.
  */
object FontScripts {
  case class PdfCharSet(height: Int, bold: Boolean, italic: Boolean, charList: Map[Int, Int])

  case class PdfFont(name: String, charSet: List[PdfCharSet], var fontSize: Int = 1)

  def getValue(list: List[String], key: String): (Int, Int) = {
    val index: Int = list.indexWhere(line => line.startsWith(key))
    if (index == -1) {
      (index, 0)
    } else {
      val line = list(index)
      val tbl = line.trim.split("""\s""")
      (index, tbl(1).toInt)
    }
  }

  def parse(fontName: String, bold: Boolean = false, italic: Boolean = false): Option[PdfCharSet] = {
    var fileFontName = fontName
    if (bold) {
      fileFontName += "-" + "Bold"
    }
    if (italic) {
      if (!fileFontName.contains("-")) {
        fileFontName += "-"
      }
      fileFontName += "Oblique"
      val file = new File(fileFontName)
      if (!file.exists()) {
        fileFontName.replace("Oblique", "Italic")
      }
    }
    try {
      //      val stream=getClass.getClassLoader.getResourceAsStream(s"./fonts/afm/$fileFontName.afm")
      //      val textList = Source.fromInputStream(stream,"UTF-16").getLines().toList
      val textList = readFile(s"./fonts/afm/adriana/$fileFontName.afm")
      val upperHeight = getValue(textList, "CapHeight")._2
      val lowerHeight = getValue(textList, "XHeight")._2
      val metrics = getValue(textList, "StartCharMetrics")
      val lineNbr = metrics._1
      val itemNbr = metrics._2
      val list1 = textList.slice(lineNbr.toInt + 1, lineNbr.toInt + 1 + itemNbr)
      val charList = list1.map(line => {
        val regExpr1 ="""C\s+(-?\d+)\s+;\s+WX\s+(\d+)\s+;\s+N\s+(\S+).*""".r
        val regExpr1(code, width, name) = line.trim
        if (code == "-1") {
          None
        } else {
          val code1 = code.toInt
          Some(code1, width.toInt)
        }
      }).filter(elem => elem != None).map(elem => elem.get).toMap

      Some(PdfCharSet(upperHeight, bold, italic, charList))
    } catch {
      case e: Exception => {
        println("Font:" + fileFontName)
        e.printStackTrace()
        None
      }

    }
  }

  def readFile(fileName: String): List[String] = {
    import akka.actor.ActorSystem
    import akka.stream.ActorMaterializer

    import scala.concurrent.duration.Duration

    val config = ConfigFactory.parseString(
      """akka.log-dead-letters=off
      akka.log-dead-letters-during-shutdown=off """)
    implicit val system = ActorSystem("Test", config)

    implicit val materializer = ActorMaterializer()

    val stream = getClass.getClassLoader.getResource(fileName)
    val file = Paths.get(stream.getPath.toString)
    val rawData = FileIO.fromPath(file)
    val data = rawData.via(Framing.delimiter(ByteString(System.lineSeparator), 10000,
      allowTruncation = true)).map(bs => bs.utf8String).runWith(Sink.seq)
    val list = Await.result(data, Duration.Inf)
    system.terminate
    list.toList
  }

  def parseFont(fontName: String): PdfFont = {
    val list = List(parse(fontName), parse(fontName, true)) //, parse(fontName, false, true), parse(fontName, true, true))
    val list1 = list.filter(elem => elem != None).map(elem => elem.get)
    PdfFont(fontName, list1)
  }

  def generateFontScript(fontName: String): Unit = {
    val pdfFont = parseFont(fontName)
    pdfFont.charSet.foreach(item => {
      val bold = if (item.bold) 1 else 0
      val italic = if (item.italic) 1 else 0
      val fontTitle = fontName + (if (item.italic) "-Italic" else "")
      val charList = item.charList
      val keySorted = charList.keySet.toList.sortBy(key => key)
      keySorted.foreach(key => {
        val width1 = charList(key)
        val width = width1
        //math.ceil(width1/32).toInt
        val str =
          s"""
             |Add_ReportFont('${fontTitle}',${bold},${key},${width});
             |""".stripMargin.trim
        println(str)
      })
    })

  }

  def main(args: Array[String]): Unit = {
    generateFontScript("Roboto")
  }

}
