package com.sysalto.report.examples

import com.sysalto.render.{PdfITextFactory, PdfNativeFactory}
import com.sysalto.report.template.ReportApp
import com.sysalto.report.Implicits._
import com.sysalto.report.reportTypes.{RCell, ReportPageOrientation}

/**
  * Created by marian on 4/1/17.
  */
object TestReport extends {

  def run1(): Unit = {
    implicit val pdfITextFactory = new PdfITextFactory()
    val report = Report("Test1.pdf")
    runReport(report)
  }

  def run2(): Unit = {
    implicit val pdfITextFactory = new PdfNativeFactory()
    val report = Report("Test2.pdf",ReportPageOrientation.PORTRAIT)
    runReport(report)
  }

  def runReport(report: Report): Unit = {
    report.nextLine()
    report print (RCell(("AAA III" size 8)+(" WWW rrr" size 12) +" iii "+( "uuu" bold()))  between RMargin(0, 50))


//    report print "test1".bold() at 10
//    report.nextLine(3)
//    report line() from(10, report.getY) to (report.pgSize.width - 10) draw()
//    report.nextLine()
//    report print "test1".size(15) at 40
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

    report.render()
    report.close()
  }


  def main(args: Array[String]): Unit = {
    run1()
    run2()
  }

}
