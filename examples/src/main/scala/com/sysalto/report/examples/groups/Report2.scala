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




package com.sysalto.report.examples.groups

import akka.stream.scaladsl.Source
import com.sysalto.render.PdfNativeFactory
import com.sysalto.report.Implicits._
import com.sysalto.report.ImplicitsAkka._
import com.sysalto.report.akka.template.ReportAppAkka
import com.sysalto.report.akka.util.AkkaGroupUtil
import com.sysalto.report.reportTypes.GroupUtil
import com.sysalto.report.util.ImplicitsExample._


object Report2 extends ReportAppAkka with AkkaGroupUtil {
  private def run(): Unit = {
    implicit val pdfFactory = new PdfNativeFactory()
    val report = Report("report2.pdf")

    report.getFooterSize = { _ =>
      30
    }

    report.footerFct = {
      case ( pgNbr, pgMax) =>
        report.setYPosition(report.pgSize.height - report.lineHeight * 2)
        report line() from(10, report.getY) to (report.pgSize.width - 10) draw()
        report.setYPosition(report.getY + report.lineHeight * 0.5f)
        report print (RCell(s"Page $pgNbr of $pgMax" bold()) rightAlign() inside RMargin(0, report.pgSize.width - 10))
    }

    val account = List(DataField("id", NumericType), DataField("accountName", StringType, 20), DataField("planType",
      StringType, 10))

    val accountResultSet = account withGroup List(DataGroup("region", 2), DataGroup("branch", 4),
      DataGroup("agent", 4)) havingLength 14
    val accountSource = Source(accountResultSet)
    val accountGroup = List(Group("region", (r: Map[String, String]) => r("region")), Group("branch",
      (r: Map[String, String]) => r("branch")),
      Group("agent", (r: Map[String, String]) => r("agent")))
    val accountGroupUtil = new GroupUtil(accountGroup)

    val row = Row(10, report.pgSize.width - 10, List(Column("accountNbr", Flex(1)), Column("accountNme", Flex(1)),
      Column("planType", Flex(1))))
    val accountNbrC = row.getColumnBound("accountNbr")
    val accountNmeC = row.getColumnBound("accountNme")
    val planTypeC = row.getColumnBound("planType")

    val h_accountNbr = RCell("Account Number" bold()) leftAlign() inside accountNbrC
    val h_accountNme = RCell("Account name" bold()) leftAlign() inside accountNmeC
    val h_planType = RCell("Plan Type" bold()) rightAlign() inside planTypeC
    val hrow = RRow(List(h_accountNbr, h_accountNme, h_planType))


    report.nextLine()

    var agentFirstItem: Option[ReportCheckpoint] = None

    val result1 = accountSource.group.
      runWith(Sink.foreach(
        rec1 => {
          val accountRec = GroupUtil.getRec(rec1)
          val isHeader = accountGroupUtil.isHeader("agent", rec1)
          var newPageForAgent = false
          if (!GroupUtil.isFirstRecord(rec1) && accountGroupUtil.isHeader("branch", rec1)) {
            report.nextPage()
            newPageForAgent = true
          }
          if (GroupUtil.isFirstRecord(rec1)) {
            newPageForAgent = true
          }
          if (accountGroupUtil.isHeader("region", rec1)) {
            report.text("Region:" + accountRec("region"), 10)
            report.nextLine()
          }
          if (accountGroupUtil.isHeader("branch", rec1)) {
            report.text("Branch:" + accountRec("branch"), 10)
            report.nextLine()
          }
          if (accountGroupUtil.isHeader("agent", rec1)) {
            if (!newPageForAgent) {
              report.nextLine(2)
              agentFirstItem = Some(report.checkpoint())
            } else {
              agentFirstItem = None
            }
            report.text("Agent:" + accountRec("agent"), 10)
            report.nextLine()
            hrow.print(report)
            report line() from(10, report.getY) to (report.pgSize.width - 10) draw()
            report.nextLine()
            report line() from(10, report.getY) to (report.pgSize.width - 10) draw()
            report.nextLine()
          }

          val accountNbr = RCell(accountRec("id")) leftAlign() inside accountNbrC
          val accountNme = RCell(accountRec("accountName")) leftAlign() inside accountNmeC
          val planType = RCell(accountRec("planType")) rightAlign() inside planTypeC
          val row = RRow(List(accountNbr, accountNme, planType))
          if (report.lineLeft < 5) {
            val toPast = if (agentFirstItem.isDefined) {
              // if agent didn't start from the begining of page , move it from the old page to the new page
              report.cut(agentFirstItem.get)
            } else {
              ReportCut(0f, List[ReportItem]())
            }
            report.nextPage()
            if (!isHeader && agentFirstItem.isEmpty) {
              report.nextLine()
              hrow.print(report)
              report line() from(10, report.getY) to (report.pgSize.width - 10) draw()
              report.nextLine()
              report line() from(10, report.getY) to (report.pgSize.width - 10) draw()
              report.nextLine()
            }
            report.paste(agentFirstItem.get, toPast)
          }
          row.print(report)

          report.nextLine()

        }
      ))
    Await.ready(result1, Duration.Inf)


    report.render()
    system.terminate()

  }


  def main(args: Array[String]): Unit = {
    run()
  }

}
