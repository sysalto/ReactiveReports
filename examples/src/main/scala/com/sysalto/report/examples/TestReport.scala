package com.sysalto.report.examples

import com.sysalto.render.{PdfITextFactory, PdfNativeFactory}
import com.sysalto.report.template.ReportApp
import com.sysalto.report.Implicits._
import com.sysalto.report.reportTypes.{RCell, ReportPageOrientation}

/**
  * Created by marian on 4/1/17.
  */
object TestReport  {

  def run1(): Unit = {
    implicit val pdfITextFactory = new PdfITextFactory()
    val report = Report("Test1.pdf")
    runReport(report)
  }

  def run2(): Unit = {
    implicit val pdfFactory = new PdfNativeFactory()
    val report = Report("Test2.pdf" ,ReportPageOrientation.LANDSCAPE)
    runReport(report)
  }

  def runReport(report: Report): Unit = {
    report.nextLine(3)
//    report print (RCell(("line1 \nline2" size 8)+(" WW line 3" size 12) +" iii"+( "uuu line4" bold()))  between RMargin(0, 50))
//    report.nextLine()
//
    report print "line1".bold() at 100
//    report.nextLine(3)
//    report line() from(10, report.getY) to (report.pgSize.width - 10) draw()
//    report.nextLine()
//    report print "line2".size(15) at 200
//    report.nextLine()
//    report print "test2" at 10
//    report.newPage()
//    report.nextLine(2)
//    report print "Page 2 test1" at 10
//    report.nextLine()
//    report print "Page 2 test2".bold() at 10
//    report.nextLine()
//    report print "Page 2 test3".italic() at 10
//    report.nextLine()
//    report print "Page 2 test4".bold().italic() at 10

//    val headerColor = RColor(156, 76, 6)
//    val headerFontColor = RColor(255, 255, 255)
//    report rectangle() from(9, report.getY-3*report.lineHeight) to(report.pgSize.width - 9, report.getY+100) fillColor headerColor draw()
//    report print ("Page 2 test2".bold() color headerFontColor) at 10



//    report rectangle() from(9,165) to(783,197) draw()
    report rectangle() from(0, 0) to(100,100) verticalShade(RColor(0, 255, 255), RColor(255, 255, 180)) draw()
//    report rectangle() from(100, 100) to(200,200) verticalShade(RColor(0, 0, 255), RColor(255, 255, 255)) draw()
   // report.drawImage("examples/src/main/resources/images/bank_banner.jpg", 5, 100, 100, 100)
//    report rectangle() from(0, 0) to(report.pgSize.width, report.pgSize.height) verticalShade(RColor(255, 255, 255), RColor(255, 255, 180)) draw()

    report.render()
    report.close()
  }


  def main(args: Array[String]): Unit = {
    run1()
    run2()
  }

}
