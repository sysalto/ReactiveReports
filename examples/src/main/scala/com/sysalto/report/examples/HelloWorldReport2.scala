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


object HelloWorldReport2 extends ReportAppAkka with AkkaGroupUtil {
  implicit val pdfNativeFactory = new PdfNativeFactory()
  private def run(): Unit = {

    // setup a new report with the name
    val report = Report("HelloWorld.pdf")

    // generate 200 records for printing
    case class Record(city: String, name: String, address: String)
    val records = for (i <- 1 to 25) yield {
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



    report.nextLine()
    var done = false
    //execute the report loop
    val result1 = reportSource.group.
      runWith(Sink.foreach(
        rec1 => try {
          val currentRecord = GroupUtil.getRec(rec1)
          report.newPage()
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
    system.terminate()

  }


  def main(args: Array[String]): Unit = {
    run()
  }

}
