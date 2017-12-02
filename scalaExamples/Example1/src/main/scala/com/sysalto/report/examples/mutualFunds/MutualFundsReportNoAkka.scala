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




package com.sysalto.report.examples.mutualFunds

import java.nio.file.Paths
import java.text.SimpleDateFormat
import java.util.GregorianCalendar

import com.sysalto.render.PdfNativeFactory
import com.sysalto.report.Implicits._
import com.sysalto.report.reportTypes.{GroupUtil, ReportPageOrientation}
import com.sysalto.report.util.ResultSetUtilTrail

import scala.collection.mutable.ListBuffer

object MutualFundsReportNoAkka extends ResultSetUtilTrail {
	val sd = new SimpleDateFormat("MMM dd yyyy")
	private val date1 = new GregorianCalendar(2013, 0, 1).getTime
	private val date2 = new GregorianCalendar(2013, 11, 31).getTime
	val headerColor = RColor(156, 76, 6)
	val headerFontColor = RColor(255, 255, 255)

	private def drawbackgroundImage(report: Report): Unit = {
		report rectangle() from(0, 0) to(report.pgSize.width, report.pgSize.height) verticalShade(RColor(255, 255, 255), RColor(255, 255, 180)) draw()
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

		report print (RCell("Investment statement" size 15 bold()) rightAlign() inside RMargin(0, report.pgSize.width - 10))
		report.nextLine()

		val str = sd.format(date1) + " to " + sd.format(date2)
		report print (RCell(str size 15 bold()) rightAlign() inside RMargin(0, report.pgSize.width - 10))
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
		report print (RCell("Beneficiary information" bold()) rightAlign() inside RMargin(0, report.pgSize.width - 10))
		report.nextLine()
		report print (RCell((record value "benef_name").toString) rightAlign() inside RMargin(0, report.pgSize.width - 10))
		report.nextLine(2)
	}


	private def summaryOfInvestment(report: Report): Unit = {

		report.nextLine(2)
		//    report line() from(10, report.getY) to (report.pgSize.width - 10) draw()
		val row = Row(10, report.pgSize.width - 10, List(Column("fund_name", 150), Column("value1", Flex(1)),
			Column("value2", Flex(1)), Column("change", Flex(1)), Column("graphic", Flex(2)), Column("", 5)))
		val fundName = row.getColumnBound("fund_name")
		val value1 = row.getColumnBound("value1")
		val value2 = row.getColumnBound("value2")
		val change = row.getColumnBound("change")
		val graphic = row.getColumnBound("graphic")
		val c_fundName = RCell("Summary of investments" bold() color headerFontColor) leftAlign() inside fundName
		val c_value1 = RCell(s"Value on\n${sd.format(date1)}($$)" bold() color headerFontColor) rightAlign() inside value1
		val c_value2 = RCell(s"Value on\n${sd.format(date2)}($$)" bold() color headerFontColor) rightAlign() inside value2
		val c_change = RCell(s"Change($$)" bold() color headerFontColor) rightAlign() inside change
		val c_graphic = RCell(s"Assets mix\n${sd.format(date2)}(%)" bold() color headerFontColor) rightAlign() inside graphic
		val rrow = RRow(List(c_fundName, c_value1, c_value2, c_change, c_graphic))
		val y2 = rrow.calculate(report)
		report rectangle() from(9, report.getY - report.lineHeight) radius (3) to(report.pgSize.width - 9, y2 + 2) fillColor headerColor draw()
		rrow.print(report)
		report.setYPosition(y2)
		report.nextLine()


		//    report line() from(10, report.getY - report.lineHeight * 0.5f) to (report.pgSize.width - 10) color(200, 200, 200) draw()
		val rs = MutualFundsInitData.query("select * from sum_investment")
		val rsGroup = rs.toGroup
		var firstChar = 'A'.asInstanceOf[Int]
		var total1 = 0f
		var total2 = 0f
		var total3 = 0f
		var firstY = 0f
		val chartData: ListBuffer[(String, Double)] = ListBuffer()

		rsGroup.foreach(
			rec => try {
				if (GroupUtil.isFirstRecord(rec)) {
					firstY = report.getY
				}
				val crtRec = GroupUtil.getRec(rec)
				val c_fundName = RCell(RText(firstChar.asInstanceOf[Char].toString + " ").bold() + (crtRec value "fund_name").toString) leftAlign() inside fundName

				val c_value1 = RCell((crtRec value "value1").toString) rightAlign() inside value1
				val c_value2 = RCell((crtRec value "value2").toString) rightAlign() inside value2
				val val1 = (crtRec value "value1").toString
				val val2 = (crtRec value "value2").toString
				val v_change = val2.toFloat - val1.toFloat
				total1 += val1.toFloat
				total2 += val2.toFloat
				total3 += v_change
				chartData += (firstChar.asInstanceOf[Char].toString -> total2.toDouble)
				val c_change = RCell(v_change.toString) rightAlign() inside change
				val rrow = RRow(List(c_fundName, c_value1, c_value2, c_change))
				val y2 = rrow.calculate(report)
				rrow.print(report)
				if (GroupUtil.isLastRecord(rec)) {
					report line() from(10, report.getY + 2) to change.right width 0.5f draw()
				} else {
					report line() from(10, report.getY + 2) to change.right color(200, 200, 200) draw() //lineType LineDashType(2, 1) draw()
				}
				firstChar += 1
				report.nextLine()
			} catch {
				case e: Throwable =>
					e.printStackTrace()
			})
		rs.close()

		val trow = RRow(List(RCell("Total" bold()) inside fundName, RCell(total1.toString bold()) rightAlign() inside value1,
			RCell(total2.toString bold()) rightAlign() inside value2, RCell(total3.toString bold()) rightAlign() inside change))
		trow.print(report)
		val chartHeight = report.getY - firstY
		report.drawPieChart("", chartData.toList, graphic.left + 5, firstY, graphic.right - graphic.left - 10, chartHeight)

	}

	private def changeAccount(report: Report): Unit = {
		report.nextLine(2)
		val row = Row(10, report.pgSize.width - 10, List(Column("account", 250), Column("value1", Flex(1)),
			Column("value2", Flex(1)), Column("value3", Flex(1)), Column("", 5)))
		val account = row.getColumnBound("account")
		val value1 = row.getColumnBound("value1")
		val value2 = row.getColumnBound("value2")
		val value3 = row.getColumnBound("value3")
		val accountHdr = RCell("Change in the value of account" bold() color headerFontColor) leftAlign() inside account
		val value1Hdr = RCell("This period($)" bold() color headerFontColor) rightAlign() inside value1
		val value2Hdr = RCell("Year-to-date($)" bold() color headerFontColor) rightAlign() inside value2
		val value3Hdr = RCell(s"Since\n${sd.format(date1)}($$)" bold() color headerFontColor) rightAlign() inside value3
		val rrow = RRow(List(accountHdr, value1Hdr, value2Hdr, value3Hdr))
		val y2 = rrow.calculate(report)
		report rectangle() from(9, report.getY - report.lineHeight) radius (3) to(report.pgSize.width - 9, y2 + 2) fillColor headerColor draw()
		rrow.print(report)
		report.setYPosition(y2)
		report.nextLine()
		val rs = MutualFundsInitData.query("select * from tran_account")
		val rsGroup = rs.toGroup
		var total1, total2, total3 = 0f
		rsGroup.foreach(
			rec => try {
				val crtRec = GroupUtil.getRec(rec)
				val name = (crtRec value "name").toString
				val r_value1 = (crtRec value "value1").toString
				val r_value2 = (crtRec value "value2").toString
				val r_value3 = (crtRec value "value3").toString
				val c_account = RCell(name) leftAlign() inside account
				val c_value1 = RCell(r_value1) rightAlign() inside value1
				val c_value2 = RCell(r_value2) rightAlign() inside value2
				val c_value3 = RCell(r_value3) rightAlign() inside value3
				total1 += r_value1.toFloat
				total2 += r_value2.toFloat
				total3 += r_value3.toFloat

				val rrow = RRow(List(c_account, c_value1, c_value2, c_value3))
				val y2 = rrow.calculate(report)
				rrow.print(report)
				val lColor = if (GroupUtil.isLastRecord(rec)) RColor(0, 0, 0) else RColor(200, 200, 200)
				report line() from(10, report.getY + 2) to value3.right color (lColor) lineType LineDashType(2, 1) draw()
				report.nextLine()
			} catch {
				case e: Throwable =>
					e.printStackTrace()
			}
		)

		rs.close()
		val accountSum = RCell(s"Value of  account on ${sd.format(date2)}" bold()) leftAlign() inside account
		val value1Sum = RCell(total1.toString bold()) rightAlign() inside value1
		val value2Sum = RCell(total2.toString bold()) rightAlign() inside value2
		val value3Sum = RCell(total3.toString bold()) rightAlign() inside value3
		val frow = RRow(List(accountSum, value1Sum, value2Sum, value3Sum))
		val y3 = frow.calculate(report)
		frow.print(report)
		report.setYPosition(y3)
		report line() from(10, report.getY + report.lineHeight * 0.5f) to value3.right width 1.5f draw()
		report.nextLine()

	}


	private def accountPerformance(report: Report): Unit = {
		val rs = MutualFundsInitData.query("select * from account_perf")
		rs.next()
		val record = rs.toMap
		rs.close()
		val row = Row(10, report.pgSize.width - 10, List(Column("account_perf", 150), Column("value3m", Flex(1)),
			Column("value1y", Flex(1)), Column("value3y", Flex(1)), Column("value5y", Flex(1)),
			Column("value10y", Flex(1)), Column("annualized", Flex(1)), Column("", 5)))
		val accountPerf = row.getColumnBound("account_perf")
		val value3m = row.getColumnBound("value3m")
		val value1y = row.getColumnBound("value1y")
		val value3y = row.getColumnBound("value3y")
		val value5y = row.getColumnBound("value5y")
		val value10y = row.getColumnBound("value10y")
		val annualized = row.getColumnBound("annualized")
		report.nextLine(3)

		val h_accountPerf = RCell("Account performance" bold() color headerFontColor) leftAlign() inside accountPerf
		val h_value3m = RCell("3 Months (%)" bold() color headerFontColor) rightAlign() inside value3m
		val h_value1y = RCell("1 Year (%)" bold() color headerFontColor) rightAlign() inside value1y
		val h_value3y = RCell("3 Years (%)" bold() color headerFontColor) rightAlign() inside value3y
		val h_value5y = RCell("5 Years (%)" bold() color headerFontColor) rightAlign() inside value5y
		val h_value10y = RCell("10 Years (%)" bold() color headerFontColor) rightAlign() inside value10y
		val h_annualized = RCell(s"Annualized since ${sd.format(date1)} (%)" bold() color headerFontColor) rightAlign() inside annualized
		val hrow = RRow(List(h_accountPerf, h_value3m, h_value1y, h_value3y, h_value5y, h_value10y, h_annualized))
		val y1 = hrow.calculate(report)
		report rectangle() from(9, report.getY - report.lineHeight) radius (3) to(report.pgSize.width - 9, y1 + 2) fillColor headerColor draw()
		hrow.print(report)
		report.setYPosition(y1)
		report.nextLine()

		val r_accountPerf = RCell("Your personal rate of return") leftAlign() inside accountPerf
		val r_value3m = RCell((record value "value3m").toString) rightAlign() inside value3m
		val r_value1y = RCell((record value "value1y").toString) rightAlign() inside value1y
		val r_value3y = RCell((record value "value3y").toString) rightAlign() inside value3y
		val r_value5y = RCell((record value "value5y").toString) rightAlign() inside value5y
		val r_value10y = RCell((record value "value10y").toString) rightAlign() inside value10y
		val r_annualized = RCell((record value "annualized").toString) rightAlign() inside annualized

		val rrow = RRow(List(r_accountPerf, r_value3m, r_value1y, r_value3y, r_value5y, r_value10y, r_annualized))
		val y2 = rrow.calculate(report)
		rrow.print(report)
		report.setYPosition(y2)
	}

	private def disclaimer(report: Report): Unit = {
		report.newPage()
		report.nextLine()
		report print (RCell("Disclaimer" bold() size 20) at 50)
		report.nextLine(2)
		val txtList =
			List("Lorem ipsum dolor sit amet, quo consul dolores te, et modo timeam assentior mei. Eos et sonet soleat copiosae. Malis labitur constituam cu cum. Qui unum probo an. Ne verear dolorem quo, sed mediocrem hendrerit id. In alia persecuti nam, cum te equidem elaboraret.",
				"Sint definiebas eos ea, et pri erroribus consectetuer. Te duo veniam iracundia. Utinam diceret efficiendi ad has. Ad mei saepe aliquam electram, sit ne nostro mediocrem neglegentur. Probo adhuc hendrerit nam at, te eam exerci denique appareat.",
				"Eu quem patrioque his. Brute audire equidem sit te, accusam philosophia at vix. Ea invenire inimicus prodesset his, has sint dicunt quaerendum id. Mei reque volutpat quaerendum an, an numquam graecis fierent mel, vim nisl soleat vivendum ut. Est odio legere saperet ad. Dolor invidunt in est.",
				"Porro accumsan lobortis no mea, an harum impetus invenire mei. Sed scaevola insolens voluptatibus ad. Eu aeque dicunt lucilius sit, no nam nullam graecis. Ad detracto deserunt cum, qui nonumy delenit invidunt ne. Per eu nulla soluta verear, in purto homero phaedrum vel, usu ut quas deserunt. Sed abhorreant neglegentur ea, tantas dicunt aliquam mei eu.",
				"Dico fabulas ea est, oporteat scribentur cum ea, usu at nominati reprimique. His omnes saperet eu, nec ei mutat facete vituperatoribus. Ius in erant eirmod fierent, nec ex melius tincidunt. Assueverit interesset vel cu, dicam offendit cu pro, natum atomorum omittantur vim ea. Alii eleifend pri at, an autem nonumy est. Alterum suavitate ea has, dicam reformidans sed no.",
				"Per iriure latine regione ei, libris maiorum sensibus ne qui, te iisque deseruisse nam. Cu mel doming ocurreret, quot rebum volumus an per. Nec laudem partem recusabo in, ei animal luptatum mea. Atqui possim deterruisset qui at, cu dolore intellegebat vim. Sit ad intellegebat vituperatoribus, eu dolores salutatus qui, mei at suas option suscipit. Veniam quodsi patrioque cu qui, ornatus voluptua neglegentur cum eu.",
				"Ea sit brute atqui soluta, qui et mollis eleifend elaboraret. Nec ex tritani repudiare. Ne ornatus salutandi disputationi eos. Sed possit omnesque disputationi et, nominavi recusabo vix in, tota recusabo sententiae et cum. Mei cu ipsum euripidis philosophia, vel homero verterem instructior ex.",
				"Ea affert tation nemore mea. Eum oratio invenire accommodare in, at his lorem atqui iriure, ei alii feugait interesset vel. No per tollit detraxit forensibus. Duo ad nonumy officiis argumentum, sea persius moderatius et.",
				"Pro stet oratio exerci in. Per no nullam salutatus scriptorem. Stet alterum nam ei, congue tamquam sed ea. Eam ut virtute disputationi, ea labitur voluptua has. Est ea graecis definitiones, pro ea mutat oportere adipiscing.",
				"Suscipit ponderum verterem et mel, vim semper facilisi ex, mel aliquid constituam ut. Summo denique complectitur ius at, in quo nobis deterruisset. Ut viris convenire eam. Quo id suscipit quaerendum, magna veniam et vix, duis liber disputando et has. Aliquando democritum id usu, falli diceret invidunt in per, in falli essent quo.",
				"Dico fabulas ea est, oporteat scribentur cum ea, usu at nominati reprimique. His omnes saperet eu, nec ei mutat facete vituperatoribus. Ius in erant eirmod fierent, nec ex melius tincidunt. Assueverit interesset vel cu, dicam offendit cu pro, natum atomorum omittantur vim ea. Alii eleifend pri at, an autem nonumy est. Alterum suavitate ea has, dicam reformidans sed no.",
				"Per iriure latine regione ei, libris maiorum sensibus ne qui, te iisque deseruisse nam. Cu mel doming ocurreret, quot rebum volumus an per. Nec laudem partem recusabo in, ei animal luptatum mea. Atqui possim deterruisset qui at, cu dolore intellegebat vim. Sit ad intellegebat vituperatoribus, eu dolores salutatus qui, mei at suas option suscipit. Veniam quodsi patrioque cu qui, ornatus voluptua neglegentur cum eu.",
				"Ea sit brute atqui soluta, qui et mollis eleifend elaboraret. Nec ex tritani repudiare. Ne ornatus salutandi disputationi eos. Sed possit omnesque disputationi et, nominavi recusabo vix in, tota recusabo sententiae et cum. Mei cu ipsum euripidis philosophia, vel homero verterem instructior ex.")
		txtList.foreach(txt => {
			val cell = RCell(txt) inside RMargin(10, report.pgSize.width - 10)
			val box = cell.calculate(report)
			report print cell
			report.setYPosition(box.currentY + report.lineHeight)
			if (report.lineLeft < 10) {
				report.newPage()
			}
			//          report.nextLine()
		})

	}


	private def report(report: Report): Unit = {
		report.getHeaderSize = { pgNbr =>
			if (pgNbr == 1) 0 else 50
		}

		report.getFooterSize = { _ =>
			30
		}

		report.headerFct = {
			case (rpt, _, _) =>
				rpt.setYPosition(10)
				val row = Row(10, rpt.pgSize.width - 10, List(Column("column1", Flex(1)), Column("column2", Flex(1)),
					Column("column3", Flex(1))))
				val column1 = row.getColumnBound("column1")
				val column2 = row.getColumnBound("column2")
				val column3 = row.getColumnBound("column3")

				val h_column1 = RCell("Type of Account" bold()) leftAlign() inside column1
				val h_column2 = RCell("Your account number" bold()) leftAlign() inside column2
				val h_column3 = RCell("Your investment statement" bold()) rightAlign() inside column3
				val hrow = RRow(List(h_column1, h_column2, h_column3))
				hrow.print(rpt)

				rpt.nextLine()

				val str = sd.format(date1) + " to " + sd.format(date2)
				val r_column1 = RCell("Group Registered Retirement Saving Plan") leftAlign() inside column1
				val r_column2 = RCell("123456789") leftAlign() inside column2
				val r_column3 = RCell(str) rightAlign() inside column3
				val rrow = RRow(List(r_column1, r_column2, r_column3))
				rrow.print(rpt)
				rpt.nextLine(2)
				rpt line() from(10, rpt.getY) to (rpt.pgSize.width - 10) draw()
				drawbackgroundImage(report)
		}

		report.footerFct = {
			case (rpt, pgNbr, pgMax) =>
				rpt.setYPosition(rpt.pgSize.height - rpt.lineHeight * 3)
				rpt line() from(10, rpt.getY) to (rpt.pgSize.width - 10) draw()
				rpt.nextLine()
				rpt print (RCell(s"Page $pgNbr of $pgMax" bold()) rightAlign() inside RMargin(0, report.pgSize.width - 10))
		}

		reportHeader(report)
		summaryOfInvestment(report)
		changeAccount(report)
		accountPerformance(report)
		disclaimer(report)
		report.render()
		report.close()

	}


	def runNative(): Unit = {
		implicit val pdfFactory = new PdfNativeFactory()
		val report1 = Report("MutualFunds2.pdf", ReportPageOrientation.LANDSCAPE)
		report(report1)
	}

	def main(args: Array[String]): Unit = {
		MutualFundsInitData.initDb()
		runNative
	}
}
