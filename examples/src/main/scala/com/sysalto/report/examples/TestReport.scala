/*
 * ReactiveReports - Free Java /Scala Reporting Library.
 * Copyright (C) 2017 SysAlto Corporation. All rights reserved.
 *
 * This program is part of ReactiveReports.
 *
 * ReactiveReports is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ReactiveReports is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY. Without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with ReactiveReports.
 * If not, see https://www.gnu.org/licenses/lgpl-3.0.en.html.
 */



package com.sysalto.report.examples

import com.sysalto.render.PdfNativeFactory
import com.sysalto.report.Implicits._
import com.sysalto.report.akka.template.ReportAppAkka
import com.sysalto.report.reportTypes.{ReportCell, ReportPageOrientation}


object TestReport  {



  def run2(): Unit = {
    implicit val pdfFactory = new PdfNativeFactory()
    val report = Report("Test2.pdf" ,ReportPageOrientation.LANDSCAPE,null,false)
    runReport(report)
  }

  def runReport(report: Report): Unit = {
    report.nextLine(3)
    report print "line1".size(30) at 100
//    report.nextLine
//    report print "line1".bold() at 100
//    report.nextLine
//    report print "line1".bold().italic().size(20) at 100
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
//    report rectangle() from(100, 100) to(200,200) radius(5) verticalShade(RColor(0, 255, 255), RColor(255, 255, 180)) draw()
//    report rectangle() from(100, 100) to(200,200) verticalShade(RColor(0, 0, 255), RColor(255, 255, 255)) draw()
   // report.drawImage("examples/src/main/resources/images/bank_banner.jpg", 5, 100, 100, 100)
//    report rectangle() from(0, 0) to(report.pgSize.width, report.pgSize.height) verticalShade(RColor(255, 255, 255), RColor(255, 255, 180)) draw()
//    val chartData=Map("A"->20.0,"B"->30.0,"C"->50.0,"D"->30.0,"E"->10.0,"F"->15.0)
//    report.drawPieChart("", chartData.toList, 100,100,100,300)
//    report.drawImage("examples/src/main/resources/images/bank_banner.jpg", 5, 45, 100, 40)
    report.render()
  }


  def main(args: Array[String]): Unit = {
//    run1()
    run2()
  }

}
