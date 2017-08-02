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




package com.sysalto.report.examples.rss

import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.stream.{FlowShape, SourceShape}
import akka.stream.scaladsl.{Sink, Source, _}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}
import GraphDSL.Implicits._
import com.sysalto.render.PdfNativeFactory
import com.sysalto.report.Implicits._
import com.sysalto.report.akka.template.ReportAppAkka
import com.sysalto.report.akka.util.AkkaGroupUtil
import com.sysalto.report.reportTypes.GroupUtil
import com.sysalto.report.util.ResultSetUtil.ReportRecord



object RssReport extends ReportAppAkka with AkkaGroupUtil{


  type RssType = (String, String, String, String)

  type RssReportType = ReportRecord[RssType]

  val headerColor = RColor(240, 250, 255)

  private def webCall(url: String): String = {
    val response = Http().singleRequest(HttpRequest(uri = url))
    val content = for {
      r <- response
      d1 <- r.entity.dataBytes.map(_.utf8String).runFold("")(_ + _)
    } yield {
      d1
    }
    Await.ready(content, Duration.Inf)
    content.value.get.get
  }

  private def readRss(url: String) = {
    import scala.xml._
    val future = Future {
      val str = webCall(url)
      val xml = XML.loadString(str)
      val itemList = xml \\ "item"
      val chanell = (xml \\ "channel" \ "title").text
      val list = itemList.map(item => (chanell, (item \ "title").text, (item \ "description").text, (item \ "link").text))
      list.toList
    }
    akka.stream.scaladsl.Source.fromFuture(future).flatMapConcat(f => akka.stream.scaladsl.Source(f)).async
  }


  private def runReports(): Unit = {
    val source1 = readRss("http://rss.cnn.com/rss/edition_us.rss")
    val source2 = readRss("http://rss.cnn.com/rss/edition_europe.rss")
    val source3 = readRss("http://rss.cnn.com/rss/edition_americas.rss")
    val source = Source.fromGraph(GraphDSL.create() {
      implicit builder =>
        val merge = builder.add(Merge[RssType](3))
        source1 ~> merge
        source2 ~> merge
        source3 ~> merge
        SourceShape(merge.out)
    })

    implicit val pdfITextFactory = new PdfNativeFactory()
    val reportAmerica = Report("AmericaRss.pdf")
    val reportEuro = Report("EuropeRss.pdf")

    def footerSizeFct(pg: Long) = 30f

    def footerRptFct(report: Report)( pgNbr: Long, pgMax: Long): Unit = {
      report.setYPosition(report.pgSize.height - report.lineHeight * 3)
      report line() from(10, report.getY) to (report.pgSize.width - 10) draw()
      report.nextLine()
      report print (RCell(s"Page $pgNbr of $pgMax" bold()) rightAllign() between RMargin(0, report.pgSize.width - 10))
    }

    reportAmerica.getFooterSize = footerSizeFct
    reportEuro.getFooterSize = footerSizeFct

    reportAmerica.footerFct = footerRptFct(reportAmerica)
    reportEuro.footerFct = footerRptFct(reportEuro)

    val americaFilter = Flow[RssType].filter { case (rssSrc, _, _, _) => rssSrc.contains("US") || rssSrc.contains("Americas") }
    val euroFilter = Flow[RssType].filter { case (rssSrc, _, _, _) => rssSrc.contains("Europe") }

    def reportFct(report: Report, rec: RssReportType): Unit = {
      try {
        val crtRec = GroupUtil.getRec(rec)
        report.nextLine(2)
        report rectangle() from(9, report.getY) to(report.pgSize.width - 9, report.getY + report.lineHeight) fillColor headerColor draw()
        report print crtRec._2 at 10
        report.nextLine()
        val txt1 = crtRec._3
        val img = "<img"
        val txt = (if (txt1.contains(img)) txt1.substring(0, txt1.indexOf(img)) else txt1).trim

        if (!txt.isEmpty) {
          val details = RCell(txt) between RMargin(10, report.pgSize.width - 10)
          val wrapBox = report calculate details
          report print details
          report.setYPosition(wrapBox.currentY)
        }
        if (report.lineLeft < 10) {
          report.newPage()
        }
        if (GroupUtil.isLastRecord(rec)) {
          report.render()
        }
      }
      catch {
        case e: Throwable =>
          e.printStackTrace()
          system.terminate()
      }
    }

    val americaSink = Sink.foreach[RssReportType](reportFct(reportAmerica, _))
    val euroSink = Sink.foreach[RssReportType](reportFct(reportEuro, _))


    val flow = Flow.fromGraph(GraphDSL.create() {
      implicit builder =>
        val bcast = builder.add(Broadcast[RssType](3))
        val js = builder.add(Flow[RssType].map(f => f))
        bcast ~> americaFilter.group ~> americaSink
        bcast ~> euroFilter.group ~> euroSink
        bcast ~> js.in
        FlowShape(bcast.in, js.out)
    })
    val aa1 = source via flow runWith Sink.ignore
    Await.ready(aa1, Duration.Inf)
    system.terminate()
  }


  def main(args: Array[String]): Unit = {
    runReports()
  }
}
