package performance.scala

import java.text.SimpleDateFormat
import java.util.GregorianCalendar

import com.sysalto.render.PdfNativeFactory
import com.sysalto.report.Implicits._
import com.sysalto.report.ReportChart
import example.mutualFundsNoAkka
import com.sysalto.report.reportTypes.{CellAlign, GroupUtil, RFont, RFontFamily, ReportColor, ReportPageOrientation}
import com.sysalto.report.util._

import scala.collection.mutable.ListBuffer

object TestImages {

  def report(): Unit = {
    implicit val pdfFactory: PdfFactory = new PdfNativeFactory()
    val imageFolder = "examples/src/test/resources/images/"
    val report = Report("examples/src/test/scala/performance/scala/TestImages.pdf")
    val row = ReportRow(50, report.pageLayout.width - 10, Column("text1", Flex(4)), Column("text2", Flex(1)))
    val margin1 = row.getColumnBound("text1")
    val margin2 = row.getColumnBound("text2")
    val color = ReportColor(220, 225, 220)
    for (i <- 1 to 20000) {
      report.nextLine()
      val imageName = imageFolder + s"icon${i % 10}.jpg"
      report.drawImage(imageName, 19, report.getY, 10f, 10f)
      //      val cell1 = ReportCell(s"Text1 ${i}")  inside margin1 leftAlign()
      val cell1 = ReportCell(("W" * 100) + s"Text1 ${i}").inside(margin1). leftAlign()
      val cell2 = ReportCell(s"Text2 ${i}") .inside(margin2) .rightAlign()
      val contentRow = List(cell1, cell2)
      val y2 = report.calculate(contentRow)

      //      report rectangle() from(cell2.margin.left,report.getY)  to(cell2.margin.right,report.getY-10) fillColor color draw()
      report.drawRectangle(cell2.margin.left, report.getY, cell2.margin.right, report.getY - 10, 0, None, Some(color))

      report.print(contentRow)
      report.setYPosition(y2)
      if (report.lineLeft < 10) {
        report.nextPage()
      }
    }

    report.render()
  }

  def report1(): Unit = {
    implicit val pdfFactory: PdfFactory = new PdfNativeFactory()
    val report = Report("examples/src/test/scala/performance/scala/TestImages.pdf",pdfCompression = false)
    val margin1 = ReportMargin(100,180)
    report.nextLine()
    val cell1 = ReportCell(ReportTxt("A").bold() +"123 4"+ReportTxt("BCW DEWW").size(20).italic()+" end"). inside(margin1).leftAlign()
//    val cell1 = ReportCell(ReportTxt("ABCD")) inside margin1 leftAlign()

    val contentRow = List(cell1)
    val y2 = report.calculate(contentRow)
    report.rectangle().from(100, report.getY-report.lineHeight).  to(180, y2).color(ReportColor(0,0,0)).draw()
    report.print(contentRow)
    report.render()
  }

  def main(args: Array[String]): Unit = {
    val t1 = System.currentTimeMillis
    report
    val t2 = System.currentTimeMillis
    println("Time:" + (t2 - t1) * 0.001)
  }

}
