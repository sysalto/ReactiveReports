package example

import java.text.SimpleDateFormat
import java.util.GregorianCalendar

import com.sysalto.render.PdfNativeFactory
import com.sysalto.render.util.fonts.parsers.otf.OtfFontParser
import com.sysalto.report.Implicits._
import com.sysalto.report.ReportChart
import example.mutualFundsNoAkka
import com.sysalto.report.reportTypes.{CellAlign, GroupUtil, RFont, RFontFamily, ReportColor, ReportPageOrientation}
import com.sysalto.report.util._

import scala.collection.mutable.ListBuffer

object Test {
  def report1(): Unit = {
    implicit val pdfFactory: PdfFactory = new PdfNativeFactory()
    val report = Report("examples/src/main/scala/example/Test.pdf", pdfCompression = false)
    //    val path="/home/marian/workspace/ReactiveReports/examples/src/main/scala/example/fonts/roboto/Roboto-Regular.ttf"
    //    val path="/home/marian/Downloads/ABeeZee-Regular.otf"
    //    val path="/home/marian/Downloads/NotoSansSC-Regular.otf"
//    val path = "/home/marian/Downloads/Montserrat-Regular.otf"
        val path="/home/marian/Downloads/NotoSansCJKjp-Regular.otf"
    val fontFamily = RFontFamily(name = "CJK", regular = path)
    report.setExternalFont(fontFamily)
    val font = RFont(10, fontName = "CJK", externalFont = Some(fontFamily))
    report.font = font
    report.nextLine(10)
        val char="这是包装纸测试这是包装纸测试"
//        println("CharToInt:"+char.toLong)
    //    val char="A"
    //    val char="General windows effects not are drawing man garrets asdsa qweqew qwewqe qwewqeqw qwewqe qwewqe."
//    val char = "General windows effects not are drawing man garrets asdsa qweqew qwewqe qwewqeqw qwewqe qwewqe."
    //    val char="Здравствуй"
    val bytes = char.getBytes
    for (b <- bytes) {
      println("dec:" + b + " hex:" + String.format("%2x", b))
    }
    println("CHAR:" + char)
    //    report print ""+char at 100
    report.nextLine
    val rc = ReportCell(char).leftAlign().inside(10, 70)
    report.print(List(rc))
    //    report.nextPage()
    //    report print "Catelus cu parul cret" at 100
    //    report.nextLine
    report.render
  }

  def report2(): Unit = {
    implicit val pdfFactory: PdfFactory = new PdfNativeFactory()
    val report = Report("examples/src/main/scala/example/Test.pdf", pdfCompression = false)
    val path = "examples/src/main/scala/example/fonts/roboto/"
    val fontFamily = RFontFamily(name = "Roboto",
      regular = path + "Roboto-Regular.ttf",
      bold = Some(path + "Roboto-Bold.ttf"),
      italic = Some(path + "Roboto-Italic.ttf"),
      boldItalic = Some(path + "Roboto-BoldItalic.ttf"))
    report.setExternalFont(fontFamily)
    val font = RFont(10, fontName = "Roboto", externalFont = Some(fontFamily))
    report.font = font
    report.nextLine(3)
    //    val char="é"
    val char = "A"
    report print "" + char at 100
    report.nextLine
    report.render
  }

  def t1(): Unit = {
    val p = new OtfFontParser("/home/marian/Downloads/Montserrat-Regular.otf")
    println(p)
  }

  def report3(): Unit = {
    implicit val pdfFactory: PdfFactory = new PdfNativeFactory()
    val report = Report("examples/src/main/scala/example/Test.pdf", pdfCompression = false)
    val path = "examples/src/main/scala/example/fonts/roboto/Roboto-Regular.ttf"
    //    val path="/home/marian/Downloads/NotoSansCJKjp-Regular.otf"
    val fontFamily = RFontFamily(name = "CJK", regular = path)
    report.setExternalFont(fontFamily)
    val font = RFont(10, fontName = "CJK", externalFont = Some(fontFamily))
    report.font = font
    report.nextLine(10)
    //    val char="㓯"
//        val char="General windows effects"
    val char = "General windows effects not are drawing man garrets asdsa qweqew qwewqe qwewqeqw qwewqe qwewqe."
    //    val char="Здравствуй"
    val bytes = char.getBytes
    for (b <- bytes) {
      println("dec:" + b + " hex:" + String.format("%2x", b))
    }
    println("CHAR:" + char)
    //    report print ""+char at 100
    report.nextLine
    val rc = ReportCell(char).leftAlign().inside(10, 100)
    report.print(List(rc))
    //    report.nextPage()
    //    report print "Catelus cu parul cret" at 100
    //    report.nextLine
    report.render
  }

  def main(args: Array[String]): Unit = {
    report1()
    //t1
  }
}
