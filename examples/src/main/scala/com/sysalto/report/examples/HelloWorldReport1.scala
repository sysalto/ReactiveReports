/*
 * ReactiveReports - Free Java /Scala Reporting Library.
 * Copyright (C) 2017 SysAlto Corporation. All rights reserved.
  *
 * Unless you have purchased a commercial license agreement from SysAlto
 * the following license terms apply:
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
import com.sysalto.report.ImplicitsAkka._
import com.sysalto.report.akka.template.ReportAppAkka
import com.sysalto.report.akka.util.AkkaGroupUtil
import com.sysalto.report.reportTypes.{GroupUtil, RCell}


object HelloWorldReport1 extends ReportAppAkka with AkkaGroupUtil {
  private def run(report: Report): Unit = {

    // setup a new report with the name


    // function for getting the report footer size - in this case it's fixed:30
    report.getFooterSize = { _ =>
      30
    }

    //print report footer
    report.footerFct = {
      case (pgNbr, pgMax) =>
        report.setYPosition(report.pgSize.height - report.lineHeight * 2)
        report line() from(10, report.getY) to (report.pgSize.width - 10) draw()
        report.setYPosition(report.getY + report.lineHeight * 0.5f)
        report print (RCell(s"Page $pgNbr of $pgMax" bold()) rightAllign() between RMargin(0, report.pgSize.width - 10))
    }

    // generate 200 records for printing
    case class Record(city: String, name: String, address: String)
    val records = for (i <- 1 to 200) yield {
      val city = i match {
        case nbr if nbr < 40 => "Toronto"
        case nbr if nbr < 150 => "Ottawa"
        case _ => "Montreal"
      }
      Record(city, "name" + i, "address" + i)
    }

    // convert the list to akka source
    val reportSource = Source(records)
    // setup the group for the report
    val reportGroup = List(Group("city", (r: Record) => r.city))
    val reportGroupUtil = new GroupUtil(reportGroup)

    // print the header
    val row = Row(10, report.pgSize.width - 10, List(Column("name", 200), Column("address", Flex(1))))
    val nameC = row.getColumnBound("name")
    val addressC = row.getColumnBound("address")

    val h_row = RCell("Name" bold()) leftAllign() between nameC
    val h_address = RCell("Address" bold()) leftAllign() between addressC
    val hrow = RRow(List(h_row, h_address))


    report.nextLine()
    var done = false
    //execute the report loop
    val result1 = reportSource.group.
      runWith(Sink.foreach(
        rec1 => try {
          val currentRecord = GroupUtil.getRec(rec1)
          val isHeader = reportGroupUtil.isHeader("city", rec1)
          var newPageForCity = false
          if (!GroupUtil.isFirstRecord(rec1) && reportGroupUtil.isHeader("city", rec1)) {
            report.newPage()
            newPageForCity = true
          }
          if (GroupUtil.isFirstRecord(rec1)) {
            newPageForCity = true
          }
          if (reportGroupUtil.isHeader("city", rec1)) {
            report.text("City:" + currentRecord.city, 10)
            report.nextLine()
            hrow.print(report)
            report line() from(10, report.getY) to (report.pgSize.width - 10) draw()
            report.nextLine()
            //            report line() from(10, report.getY) to (report.pgSize.width - 10) draw()
            //            report.nextLine()
          }

          val name = RCell(currentRecord.name) leftAllign() between nameC
          val address = RCell(currentRecord.address) leftAllign() between addressC
          val row = RRow(List(name, address))

          if (report.lineLeft < 5) {
            done = true
          }

          if (report.lineLeft < 5) {
            report.newPage()
            if (!isHeader) {
              report.nextLine()
              hrow.print(report)
              report line() from(10, report.getY) to (report.pgSize.width - 10) draw()
              report.nextLine()
              report line() from(10, report.getY) to (report.pgSize.width - 10) draw()
              report.nextLine()
            }
          }
          row.print(report)

          report.nextLine()

        } catch {
          case e: Throwable =>
            e.printStackTrace()
            system.terminate()
        })
      )
    Await.ready(result1, Duration.Inf)

    // render and close the report
    report.render()
  }

//  def runItext(): Unit = {
//    implicit val pdfITextFactory = new PdfITextFactory()
//    val report = Report("HelloWord1.pdf")
//    run(report)
//  }

  def runNative(): Unit = {
    implicit val pdfITextFactory = new PdfNativeFactory()
    val report = Report("HelloWord2.pdf")
    run(report)
  }

  def main(args: Array[String]): Unit = {
//    runItext
    runNative
    system.terminate()
  }

}
