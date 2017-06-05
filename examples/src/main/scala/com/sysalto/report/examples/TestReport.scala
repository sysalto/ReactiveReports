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
//    report rectangle() from(405,45) to(600,100) draw()
    report rectangle() from(100, 400) to(200,500) verticalShade(RColor(255, 255, 255), RColor(255, 255, 180)) draw()
   // report.drawImage("examples/src/main/resources/images/bank_banner.jpg", 5, 100, 100, 100)

    report.render()
    report.close()
  }


  def main(args: Array[String]): Unit = {
    run1()
    run2()
  }

}
