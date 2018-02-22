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




package com.sysalto.report.examples.mutualFunds

import java.nio.file.Paths
import java.text.SimpleDateFormat
import java.util.GregorianCalendar

import com.sysalto.render.PdfNativeFactory
import com.sysalto.report.Implicits._
import com.sysalto.report.ImplicitsAkka._
import com.sysalto.report.akka.template.ReportAppAkka
import com.sysalto.report.akka.util.AkkaGroupUtil
import com.sysalto.report.reportTypes.GroupUtil
import com.sysalto.report.util.ResultSetUtilTrait

object MutualFundsReport extends ReportAppAkka {
  val sd = new SimpleDateFormat("MMM dd yyyy")
  private val date1 = new GregorianCalendar(2013, 0, 1).getTime
  private val date2 = new GregorianCalendar(2013, 11, 31).getTime
  val headerColor = ReportColor(156, 76, 6)
  val headerFontColor = ReportColor(255, 255, 255)

  private def drawbackgroundImage(report: Report): Unit = {
    report rectangle() from(0, 0) to(report.pageLayout.width, report.pageLayout.height) verticalShade(ReportColor(255, 255, 255), ReportColor(255, 255, 180)) draw()
  }

  private def reportHeader(report: Report): Unit = {
    drawbackgroundImage(report)
    val rs = MutualFundsInitData.query("select * from clnt")
    rs.next()
    val record = rs.toMap
    rs.close()
    report.nextLine()
    val resource = this.getClass.getResource("/images/bank_banner.jpg")
    val file = Paths.get(resource.toURI).toFile
    report.drawImage(file.getAbsolutePath, 5, 45, 100, 40)

    //    report rectangle() from(500, report.getY) to(report.pgSize.width - 5, report.getY + 40) fillColor RColor(220, 255, 220) color RColor(200, 200, 250) radius 10 draw()
    report print (ReportCell("Investment statement" size 15 bold()) rightAlign() inside ReportMargin(0, report.pageLayout.width - 10))
    report.nextLine()

    val str = sd.format(date1) + " to " + sd.format(date2)
    report print (ReportCell(str size 15 bold()) rightAlign() inside ReportMargin(0, report.pageLayout.width - 10))
    report.nextLine(2)
    report print ("Mutual Funds Inc." bold()) at 10
    report.nextLine()
    report print ("Group Registered Retirement Saving Plan" bold()) at 10
    report.nextLine(2)
    val y = report.getY
    report print ((record value "name").toString bold()) at 10
    report.nextLine()
    report print (record value "addr1").toString at 10
    report.nextLine()
    report print (record value "addr2").toString at 10
    report.nextLine()
    report print (record value "addr3").toString at 10
    report.setYPosition(y)
    report print ("Beneficiary information" bold()) at 500
    report.nextLine()
    report print (record value "benef_name").toString at 500
    report.nextLine(2)
  }


  private def summaryOfInvestment(report: Report): Unit = {

    report.nextLine(2)
    //    report line() from(10, report.getY) to (report.pgSize.width - 10) draw()
    val row = ReportRow(10, report.pageLayout.width - 10, List(Column("fund_name", 150), Column("value1", Flex(1)),
      Column("value2", Flex(1)), Column("change", Flex(1)), Column("graphic", Flex(2))))
    val fundName = row.getColumnBound("fund_name")
    val value1 = row.getColumnBound("value1")
    val value2 = row.getColumnBound("value2")
    val change = row.getColumnBound("change")
    val graphic = row.getColumnBound("graphic")
    val c_fundName = ReportCell("Summary of investments" bold() color headerFontColor) leftAlign() inside fundName
    val c_value1 = ReportCell(s"Value on\n${sd.format(date1)}($$)" bold() color headerFontColor) rightAlign() inside value1
    val c_value2 = ReportCell(s"Value on\n${sd.format(date2)}($$)" bold() color headerFontColor) rightAlign() inside value2
    val c_change = ReportCell(s"Change($$)" bold() color headerFontColor) rightAlign() inside change
    val c_graphic = ReportCell(s"Assets mix\n${sd.format(date2)}(%)" bold() color headerFontColor) rightAlign() inside graphic
    val rrow = ReportCellList(List(c_fundName, c_value1, c_value2, c_change, c_graphic))
    val y2 = rrow.calculate(report)
    report rectangle() from(9, report.getY) to(report.pageLayout.width - 9, y2 + 2) fillColor headerColor draw()
    rrow.print(report)
    report.setYPosition(y2)
    report.nextLine()
    //    report line() from(10, report.getY - report.lineHeight * 0.5f) to (report.pgSize.width - 10) color(200, 200, 200) draw()
    val rs = MutualFundsInitData.query("select * from sum_investment")
    val source = rs.toSource
    var firstChar = 'A'.asInstanceOf[Int]
    var total1 = 0f
    var total2 = 0f
    var total3 = 0f
    var firstY = 0f
    val chartData: scala.collection.mutable.Map[String, Double] = scala.collection.mutable.Map()
    val result1 = source.group.
      runWith(Sink.foreach(
        rec => try {
          if (GroupUtil.isFirstRecord(rec)) {
            firstY = report.getY
          }
          val crtRec = GroupUtil.getRec(rec)
          val c_fundName = ReportCell(ReportTxt(firstChar.asInstanceOf[Char].toString + " ").bold() + (crtRec value "fund_name").toString) leftAlign() inside fundName

          val c_value1 = ReportCell((crtRec value "value1").toString) rightAlign() inside value1
          val c_value2 = ReportCell((crtRec value "value2").toString) rightAlign() inside value2
          val val1 = (crtRec value "value1").toString
          val val2 = (crtRec value "value2").toString
          val v_change = val2.toFloat - val1.toFloat
          total1 += val1.toFloat
          total2 += val2.toFloat
          total3 += v_change
          chartData += (firstChar.asInstanceOf[Char].toString -> total2.toDouble)
          val c_change = ReportCell(v_change.toString) rightAlign() inside change
          val rrow = ReportCellList(List(c_fundName, c_value1, c_value2, c_change))
          val y2 = rrow.calculate(report)
          rrow.print(report)
          report.setYPosition(y2 + 5)
          if (GroupUtil.isLastRecord(rec)) {
            report line() from(10, report.getY) to change.right width 0.5f draw()
          } else {
            report line() from(10, report.getY) to change.right color(200, 200, 200) lineType LineDashType(2, 1) draw()
          }
          firstChar += 1
        } catch {
          case e: Throwable =>
            e.printStackTrace()
            system.terminate()
        })
      )
    Await.ready(result1, Duration.Inf)
    rs.close()

    val trow = ReportCellList(List(ReportCell("Total" bold()) inside fundName, ReportCell(total1.toString bold()) rightAlign() inside value1,
      ReportCell(total2.toString bold()) rightAlign() inside value2, ReportCell(total3.toString bold()) rightAlign() inside change))
    trow.print(report)
    val chartHeight = report.getY - firstY
    report.drawPieChart("", chartData.toList, graphic.left + 5, firstY + chartHeight, graphic.right - graphic.left - 10, chartHeight)

  }

  private def changeAccount(report: Report): Unit = {
    report.nextLine(2)
    val row = ReportRow(10, report.pageLayout.width - 10, List(Column("account", 250), Column("value1", Flex(1)),
      Column("value2", Flex(1)), Column("value3", Flex(1))))
    val account = row.getColumnBound("account")
    val value1 = row.getColumnBound("value1")
    val value2 = row.getColumnBound("value2")
    val value3 = row.getColumnBound("value3")
    val accountHdr = ReportCell("Change in the value of account" bold() color headerFontColor) leftAlign() inside account
    val value1Hdr = ReportCell("This period($)" bold() color headerFontColor) rightAlign() inside value1
    val value2Hdr = ReportCell("Year-to-date($)" bold() color headerFontColor) rightAlign() inside value2
    val value3Hdr = ReportCell(s"Since\n${sd.format(date1)}($$)" bold() color headerFontColor) rightAlign() inside value3
    val rrow = ReportCellList(List(accountHdr, value1Hdr, value2Hdr, value3Hdr))
    val y2 = rrow.calculate(report)
    report rectangle() from(9, report.getY) to(report.pageLayout.width - 9, y2 + 2) fillColor headerColor draw()
    rrow.print(report)
    report.setYPosition(y2)
    report.nextLine()
    //    report line() from(10, report.getY - report.lineHeight * 0.5f) to (report.pgSize.width - 10) color(200, 200, 200) draw()
    val rs = MutualFundsInitData.query("select * from tran_account")
    val source = rs.toSource
    var total1, total2, total3 = 0f
    val result1 = source.group.
      runWith(Sink.foreach(
        rec => try {
          val crtRec = GroupUtil.getRec(rec)
          val name = (crtRec value "name").toString
          val r_value1 = (crtRec value "value1").toString
          val r_value2 = (crtRec value "value2").toString
          val r_value3 = (crtRec value "value3").toString
          val c_account = ReportCell(name) leftAlign() inside account
          val c_value1 = ReportCell(r_value1) rightAlign() inside value1
          val c_value2 = ReportCell(r_value2) rightAlign() inside value2
          val c_value3 = ReportCell(r_value3) rightAlign() inside value3
          total1 += r_value1.toFloat
          total2 += r_value2.toFloat
          total3 += r_value3.toFloat

          val rrow = ReportCellList(List(c_account, c_value1, c_value2, c_value3))
          val y2 = rrow.calculate(report)
          rrow.print(report)
          report.setYPosition(y2 + 5)
          report line() from(10, report.getY) to value3.right color(200, 200, 200) lineType LineDashType(2, 1) draw()
        } catch {
          case e: Throwable =>
            e.printStackTrace()
            system.terminate()
        }
      ))

    Await.ready(result1, Duration.Inf)
    rs.close()
    val accountSum = ReportCell(s"Value of  account on ${sd.format(date2)}" bold()) leftAlign() inside account
    val value1Sum = ReportCell(total1.toString bold()) rightAlign() inside value1
    val value2Sum = ReportCell(total2.toString bold()) rightAlign() inside value2
    val value3Sum = ReportCell(total3.toString bold()) rightAlign() inside value3
    val frow = ReportCellList(List(accountSum, value1Sum, value2Sum, value3Sum))
    val y3 = frow.calculate(report)
    frow.print(report)
    report.setYPosition(y3)
    //    report line() from(10, report.getY + report.lineHeight * 0.5f) to value3.right width 1.5f draw()
    report.nextLine()

  }


  private def accountPerformance(report: Report): Unit = {
    val rs = MutualFundsInitData.query("select * from account_perf")
    rs.next()
    val record = rs.toMap
    rs.close()
    val row = ReportRow(10, report.pageLayout.width - 10, List(Column("account_perf", 150), Column("value3m", Flex(1)),
      Column("value1y", Flex(1)), Column("value3y", Flex(1)), Column("value5y", Flex(1)),
      Column("value10y", Flex(1)), Column("annualized", Flex(1))))
    val accountPerf = row.getColumnBound("account_perf")
    val value3m = row.getColumnBound("value3m")
    val value1y = row.getColumnBound("value1y")
    val value3y = row.getColumnBound("value3y")
    val value5y = row.getColumnBound("value5y")
    val value10y = row.getColumnBound("value10y")
    val annualized = row.getColumnBound("annualized")

    val h_accountPerf = ReportCell("Account performance" bold() color headerFontColor) leftAlign() inside accountPerf
    val h_value3m = ReportCell("3 Months (%)" bold() color headerFontColor) rightAlign() inside value3m
    val h_value1y = ReportCell("1 Year (%)" bold() color headerFontColor) rightAlign() inside value1y
    val h_value3y = ReportCell("3 Years (%)" bold() color headerFontColor) rightAlign() inside value3y
    val h_value5y = ReportCell("5 Years (%)" bold() color headerFontColor) rightAlign() inside value5y
    val h_value10y = ReportCell("10 Years (%)" bold() color headerFontColor) rightAlign() inside value10y
    val h_annualized = ReportCell(s"Annualized since ${sd.format(date1)} (%)" bold() color headerFontColor) rightAlign() inside annualized
    val hrow = ReportCellList(List(h_accountPerf, h_value3m, h_value1y, h_value3y, h_value5y, h_value10y, h_annualized))
    val y1 = hrow.calculate(report)
    report rectangle() from(9, report.getY) to(report.pageLayout.width - 9, y1 + 2) fillColor headerColor draw()
    hrow.print(report)
    report.setYPosition(y1)
    report.nextLine()

    val r_accountPerf = ReportCell("Your personal rate of return") leftAlign() inside accountPerf
    val r_value3m = ReportCell((record value "value3m").toString) rightAlign() inside value3m
    val r_value1y = ReportCell((record value "value1y").toString) rightAlign() inside value1y
    val r_value3y = ReportCell((record value "value3y").toString) rightAlign() inside value3y
    val r_value5y = ReportCell((record value "value5y").toString) rightAlign() inside value5y
    val r_value10y = ReportCell((record value "value10y").toString) rightAlign() inside value10y
    val r_annualized = ReportCell((record value "annualized").toString) rightAlign() inside annualized

    val rrow = ReportCellList(List(r_accountPerf, r_value3m, r_value1y, r_value3y, r_value5y, r_value10y, r_annualized))
    val y2 = rrow.calculate(report)
    rrow.print(report)
    report.setYPosition(y2)
  }

  private def disclaimer(report: Report): Unit = {
    report.nextPage()
    drawbackgroundImage(report)
    report.nextLine()
    report print (ReportCell("Disclaimer" bold() size 20) at 50)
    report.nextLine(2)
    val txt =
      """Lorem ipsum dolor sit amet, quo consul dolores te, et modo timeam assentior mei. Eos et sonet soleat copiosae. Malis labitur constituam cu cum. Qui unum probo an. Ne verear dolorem quo, sed mediocrem hendrerit id. In alia persecuti nam, cum te equidem elaboraret.
        |
        |Sint definiebas eos ea, et pri erroribus consectetuer. Te duo veniam iracundia. Utinam diceret efficiendi ad has. Ad mei saepe aliquam electram, sit ne nostro mediocrem neglegentur. Probo adhuc hendrerit nam at, te eam exerci denique appareat.
        |
        |Eu quem patrioque his. Brute audire equidem sit te, accusam philosophia at vix. Ea invenire inimicus prodesset his, has sint dicunt quaerendum id. Mei reque volutpat quaerendum an, an numquam graecis fierent mel, vim nisl soleat vivendum ut. Est odio legere saperet ad. Dolor invidunt in est.
        |
        |Porro accumsan lobortis no mea, an harum impetus invenire mei. Sed scaevola insolens voluptatibus ad. Eu aeque dicunt lucilius sit, no nam nullam graecis. Ad detracto deserunt cum, qui nonumy delenit invidunt ne. Per eu nulla soluta verear, in purto homero phaedrum vel, usu ut quas deserunt. Sed abhorreant neglegentur ea, tantas dicunt aliquam mei eu.
        |
        |Dico fabulas ea est, oporteat scribentur cum ea, usu at nominati reprimique. His omnes saperet eu, nec ei mutat facete vituperatoribus. Ius in erant eirmod fierent, nec ex melius tincidunt. Assueverit interesset vel cu, dicam offendit cu pro, natum atomorum omittantur vim ea. Alii eleifend pri at, an autem nonumy est. Alterum suavitate ea has, dicam reformidans sed no.
        |
        |Per iriure latine regione ei, libris maiorum sensibus ne qui, te iisque deseruisse nam. Cu mel doming ocurreret, quot rebum volumus an per. Nec laudem partem recusabo in, ei animal luptatum mea. Atqui possim deterruisset qui at, cu dolore intellegebat vim. Sit ad intellegebat vituperatoribus, eu dolores salutatus qui, mei at suas option suscipit. Veniam quodsi patrioque cu qui, ornatus voluptua neglegentur cum eu.
        |
        |Ea sit brute atqui soluta, qui et mollis eleifend elaboraret. Nec ex tritani repudiare. Ne ornatus salutandi disputationi eos. Sed possit omnesque disputationi et, nominavi recusabo vix in, tota recusabo sententiae et cum. Mei cu ipsum euripidis philosophia, vel homero verterem instructior ex.
        |
        |Ea affert tation nemore mea. Eum oratio invenire accommodare in, at his lorem atqui iriure, ei alii feugait interesset vel. No per tollit detraxit forensibus. Duo ad nonumy officiis argumentum, sea persius moderatius et.
        |
        |Pro stet oratio exerci in. Per no nullam salutatus scriptorem. Stet alterum nam ei, congue tamquam sed ea. Eam ut virtute disputationi, ea labitur voluptua has. Est ea graecis definitiones, pro ea mutat oportere adipiscing.
        |
        |Suscipit ponderum verterem et mel, vim semper facilisi ex, mel aliquid constituam ut. Summo denique complectitur ius at, in quo nobis deterruisset. Ut viris convenire eam. Quo id suscipit quaerendum, magna veniam et vix, duis liber disputando et has. Aliquando democritum id usu, falli diceret invidunt in per, in falli essent quo.""".stripMargin
    report print (ReportCell(txt) inside ReportMargin(10, report.pageLayout.width - 10))
  }


  private def report(report:Report): Unit = {
    report.setHeaderSize = { pgNbr =>
      if (pgNbr == 1) 0 else 50
    }

    report.setFooterSize = { _ =>
      30
    }

    report.headerFct = {
      case (rpt, _,_) =>
        rpt.setYPosition(10)
        val row = ReportRow(10, rpt.pgSize.width - 10, List(Column("column1", Flex(1)), Column("column2", Flex(1)),
          Column("column3", Flex(1))))
        val column1 = row.getColumnBound("column1")
        val column2 = row.getColumnBound("column2")
        val column3 = row.getColumnBound("column3")

        val h_column1 = ReportCell("Type of Account" bold()) leftAlign() inside column1
        val h_column2 = ReportCell("Your account number" bold()) leftAlign() inside column2
        val h_column3 = ReportCell("Your investment statement" bold()) rightAlign() inside column3
        val hrow = ReportCellList(List(h_column1, h_column2, h_column3))
        hrow.print(rpt)

        rpt.nextLine()

        val str = sd.format(date1) + " to " + sd.format(date2)
        val r_column1 = ReportCell("Group Registered Retirement Saving Plan") leftAlign() inside column1
        val r_column2 = ReportCell("123456789") leftAlign() inside column2
        val r_column3 = ReportCell(str) rightAlign() inside column3
        val rrow = ReportCellList(List(r_column1, r_column2, r_column3))
        rrow.print(rpt)
        rpt.nextLine(2)
        rpt line() from(10, rpt.getY) to (rpt.pgSize.width - 10) draw()
    }

    report.footerFct = {
      case (rpt, pgNbr, pgMax) =>
        rpt.setYPosition(rpt.pgSize.height - rpt.lineHeight * 3)
        rpt line() from(10, rpt.getY) to (rpt.pgSize.width - 10) draw()
        rpt.nextLine()
        rpt print (ReportCell(s"Page $pgNbr of $pgMax" bold()) rightAlign() inside ReportMargin(0, report.pageLayout.width - 10))
    }

    reportHeader(report)
    summaryOfInvestment(report)
    changeAccount(report)
    accountPerformance(report)
    disclaimer(report)
    report.render()
    report.close()

  }


//  def runItext(): Unit = {
//    implicit val pdfITextFactory = new PdfITextFactory()
//    val report1=Report("MutualFunds1.pdf")
//    report(report1)
//  }

  def runNative(): Unit = {
    implicit val pdfFactory = new PdfNativeFactory()
    val report1=Report("MutualFunds2.pdf")
    report(report1)
  }

  def main(args: Array[String]): Unit = {
    MutualFundsInitData.initDb()
    runNative
    system.terminate()
  }
}
