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


package com.sysalto.report.example

import java.text.SimpleDateFormat
import java.util.GregorianCalendar

import com.sysalto.render.PdfNativeFactory
import com.sysalto.report.Implicits._
import com.sysalto.report.ReportChart
import com.sysalto.report.reportTypes.{CellAlign, GroupUtil, RFont, RFontFamily, ReportPageOrientation}
import com.sysalto.report.util._

import scala.collection.mutable.ListBuffer

object MutualFundsReportNoAkka extends GroupUtilTrait {
	val sd = new SimpleDateFormat("MMM dd yyyy")
	private val date1 = new GregorianCalendar(2013, 0, 1).getTime
	private val date2 = new GregorianCalendar(2013, 11, 31).getTime
	val headerColor = ReportColor(156, 76, 6)
	val headerFontColor = ReportColor(255, 255, 255)

	// draw background image as gradient
	private def drawbackgroundImage(report: Report): Unit = {
		report rectangle() from(0, 0) to(report.pageLayout.width, report.pageLayout.height) verticalShade(ReportColor(255, 255, 255), ReportColor(255, 255, 180)) draw()
	}

	private def reportHeader(report: Report): Unit = {
		val rs = data.MutualFundsInitData.query("select * from clnt")
		rs.next()

		// toMap - helper function that transform sql.ResultSet to Map[String, AnyRef] where the key is the field name
		val record = rs.toMap
		rs.close()
		report.nextLine()
		report.drawImage("examples/src/main/resources/images/bank_banner.jpg", 5, 45, 100, 40)

		report print (ReportCell("Investment statement" size 15 bold()) centerAlign() inside ReportMargin(0, report.pageLayout.width - 10))
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
		report print (ReportCell("Beneficiary information" bold()) rightAlign() inside ReportMargin(0, report.pageLayout.width - 10))
		report.nextLine()
		report print (ReportCell((record value "benef_name").toString) rightAlign() inside ReportMargin(0, report.pageLayout.width - 10))
		report.nextLine(2)
	}


	private def summaryOfInvestment(report: Report): Unit = {

		report.nextLine(2)
		//    report line() from(10, report.getY) to (report.pgSize.width - 10) draw()
		val row = ReportRow(10, report.pageLayout.width - 10, List(Column("fund_name", 150), Column("value1", Flex(1)),
			Column("value2", Flex(1)), Column("change", Flex(1)), Column("graphic", Flex(2)), Column("", 5)))
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
		val rrow = List(c_fundName, c_value1, c_value2, c_change, c_graphic)
		val y2 = report.calculate(rrow)
		val top = report.getY - report.lineHeight
		val bottom = y2 + 2
		report rectangle() from(9, top) radius (3) to(report.pageLayout.width - 9, bottom) fillColor headerColor draw()
		report.print(rrow, CellAlign.CENTER, top, bottom)
		report.setYPosition(y2)
		report.nextLine()


		//    report line() from(10, report.getY - report.lineHeight * 0.5f) to (report.pgSize.width - 10) color(200, 200, 200) draw()
		val rs = data.MutualFundsInitData.query("select * from sum_investment")
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
				val rrow = List(c_fundName, c_value1, c_value2, c_change)
				val y2 = report.calculate(rrow)
				report.print(rrow)
				if (GroupUtil.isLastRecord(rec)) {
					report line() from(10, report.getY + 2) to change.right width 1f draw()
				} else {
					report line() from(10, report.getY + 2) to change.right width 0.5f color(200, 200, 200) draw() //lineType LineDashType(2, 1) draw()
				}
				firstChar += 1
				report.nextLine()
			} catch {
				case e: Throwable =>
					e.printStackTrace()
			})
		rs.close()

		val trow = List(ReportCell("Total" bold()) inside fundName, ReportCell(total1.toString bold()) rightAlign() inside value1,
			ReportCell(total2.toString bold()) rightAlign() inside value2, ReportCell(total3.toString bold()) rightAlign() inside change)
		report.print(trow)
		val chartHeight = report.getY - firstY

		val reportChart=new ReportChart(report)
		reportChart.pieChart(report.font,"",chartData.toList, graphic.left + 5, firstY, graphic.right - graphic.left - 10, chartHeight)

//		report.drawPieChart("", chartData.toList, graphic.left + 5, firstY, graphic.right - graphic.left - 10, chartHeight)

	}

	private def changeAccount(report: Report): Unit = {
		report.nextLine(2)
		val row = ReportRow(10, report.pageLayout.width - 10, List(Column("account", 250), Column("value1", Flex(1)),
			Column("value2", Flex(1)), Column("value3", Flex(1)), Column("", 5)))
		val account = row.getColumnBound("account")
		val value1 = row.getColumnBound("value1")
		val value2 = row.getColumnBound("value2")
		val value3 = row.getColumnBound("value3")
		val accountHdr = ReportCell("Change in the value of account" bold() color headerFontColor) leftAlign() inside account
		val value1Hdr = ReportCell("This period($)" bold() color headerFontColor) rightAlign() inside value1
		val value2Hdr = ReportCell("Year-to-date($)" bold() color headerFontColor) rightAlign() inside value2
		val value3Hdr = ReportCell(s"Since\n${sd.format(date1)}($$)" bold() color headerFontColor) rightAlign() inside value3
		val rrow = List(accountHdr, value1Hdr, value2Hdr, value3Hdr)
		val y2 = report.calculate(rrow)
		val top = report.getY - report.lineHeight
		val bottom = y2 + 2
		report rectangle() from(9, top) radius (3) to(report.pageLayout.width - 9, bottom) fillColor headerColor draw()
		report.print(rrow, CellAlign.CENTER, top, bottom)
		report.setYPosition(y2)
		report.nextLine()
		val rs = data.MutualFundsInitData.query("select * from tran_account")
		val rsGroup = rs.toGroup
		var total1, total2, total3 = 0f
		rsGroup.foreach(
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

				val rrow = List(c_account, c_value1, c_value2, c_value3)
				val y2 = report.calculate(rrow)
				report.print(rrow)
				val lColor = if (GroupUtil.isLastRecord(rec)) ReportColor(0, 0, 0) else ReportColor(200, 200, 200)
				report line() from(10, report.getY + 2) to value3.right width 0.5f color (lColor) lineType LineDashType(2, 1) draw()
				report.nextLine()
			} catch {
				case e: Throwable =>
					e.printStackTrace()
			}
		)

		rs.close()
		val accountSum = ReportCell(s"Value of  account on ${sd.format(date2)}" bold()) leftAlign() inside account
		val value1Sum = ReportCell(total1.toString bold()) rightAlign() inside value1
		val value2Sum = ReportCell(total2.toString bold()) rightAlign() inside value2
		val value3Sum = ReportCell(total3.toString bold()) rightAlign() inside value3
		val frow = List(accountSum, value1Sum, value2Sum, value3Sum)
		val y3 = report.calculate(frow)
		report.print(frow)
		report.setYPosition(y3)
		report line() from(10, report.getY + report.lineHeight * 0.5f) to value3.right draw()
		report.nextLine()

	}


	private def accountPerformance(report: Report): Unit = {
		val rs = data.MutualFundsInitData.query("select * from account_perf")
		rs.next()
		val record = rs.toMap
		rs.close()
		val row = ReportRow(10, report.pageLayout.width - 10, List(Column("account_perf", 150), Column("value3m", Flex(1)),
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

		val h_accountPerf = ReportCell("Account performance" bold() color headerFontColor) leftAlign() inside accountPerf
		val h_value3m = ReportCell("3 Months (%)" bold() color headerFontColor) rightAlign() inside value3m
		val h_value1y = ReportCell("1 Year (%)" bold() color headerFontColor) rightAlign() inside value1y
		val h_value3y = ReportCell("3 Years (%)" bold() color headerFontColor) rightAlign() inside value3y
		val h_value5y = ReportCell("5 Years (%)" bold() color headerFontColor) rightAlign() inside value5y
		val h_value10y = ReportCell("10 Years (%)" bold() color headerFontColor) rightAlign() inside value10y
		val h_annualized = ReportCell(s"Annualized since ${sd.format(date1)} (%)" bold() color headerFontColor) rightAlign() inside annualized
		val hrow = List(h_accountPerf, h_value3m, h_value1y, h_value3y, h_value5y, h_value10y, h_annualized)
		val y1 = report.calculate(hrow)
		val top = report.getY - report.lineHeight
		val bottom = y1 + 2
		report rectangle() from(9, top) radius (3) to(report.pageLayout.width - 9, bottom) fillColor headerColor draw()
		report.print(hrow, CellAlign.CENTER, top, bottom)
		report.setYPosition(y1)
		report.nextLine()

		val r_accountPerf = ReportCell("Your personal rate of return") leftAlign() inside accountPerf
		val r_value3m = ReportCell((record value "value3m").toString) rightAlign() inside value3m
		val r_value1y = ReportCell((record value "value1y").toString) rightAlign() inside value1y
		val r_value3y = ReportCell((record value "value3y").toString) rightAlign() inside value3y
		val r_value5y = ReportCell((record value "value5y").toString) rightAlign() inside value5y
		val r_value10y = ReportCell((record value "value10y").toString) rightAlign() inside value10y
		val r_annualized = ReportCell((record value "annualized").toString) rightAlign() inside annualized

		val rrow = List(r_accountPerf, r_value3m, r_value1y, r_value3y, r_value5y, r_value10y, r_annualized)
		val y2 = report.calculate(rrow)
		report.print(rrow)
		report.setYPosition(y2)
	}


	private def report(report: Report): Unit = {

		// set page header size(height) at 50 and 0 (no page header) for the first page.
		report.setHeaderSize = { pgNbr =>
			if (pgNbr == 1) 0f else 50f
		}

		// set footer size(hight) at 30 for all pages.
		report.setFooterSize = { _ =>
			30f
		}

		// draw background image before rendering anything
		report.newPageFct = {
			case _ => drawbackgroundImage(report)
		}

		report.headerFct = {
			case (_, _) =>
				report.setYPosition(10)
				val row = ReportRow(10, report.pageLayout.width - 10, List(Column("column1", Flex(1)), Column("column2", Flex(1)),
					Column("column3", Flex(1))))
				val column1 = row.getColumnBound("column1")
				val column2 = row.getColumnBound("column2")
				val column3 = row.getColumnBound("column3")

				val h_column1 = ReportCell("Type of Account" bold()) leftAlign() inside column1
				val h_column2 = ReportCell("Your account number" bold()) leftAlign() inside column2
				val h_column3 = ReportCell("Your investment statement" bold()) rightAlign() inside column3
				val hrow = List(h_column1, h_column2, h_column3)
				report.print(hrow)
				report.nextLine()
				val str = sd.format(date1) + " to " + sd.format(date2)
				val r_column1 = ReportCell("Group Registered Retirement Saving Plan") leftAlign() inside column1
				val r_column2 = ReportCell("123456789") leftAlign() inside column2
				val r_column3 = ReportCell(str) rightAlign() inside column3
				val rrow = List(r_column1, r_column2, r_column3)
				report.print(rrow)
				report.nextLine(2)
				report line() from(10, report.getY) to (report.pageLayout.width - 10) draw()
		}

		report.footerFct = {
			case (pgNbr, pgMax) =>
				report.setYPosition(report.pageLayout.height - report.lineHeight * 3)
				report line() from(10, report.getY) to (report.pageLayout.width - 10) draw()
				report.nextLine()
				report print (ReportCell(s"Page $pgNbr of $pgMax" bold()) rightAlign() inside ReportMargin(0, report.pageLayout.width - 10))
		}
		val t1 = System.currentTimeMillis()
		report.start()

		reportHeader(report)
		summaryOfInvestment(report)
		changeAccount(report)
		accountPerformance(report)
		report.render()

	}


	def runReport(): Unit = {
		implicit val pdfFactory: PdfFactory = new PdfNativeFactory()

		// create report with RocksDb persistence.Otherwise can use custom persistence for example derbyPersistanceFactory
		val report1 = Report("MutualFundsReportNoAkka.pdf", ReportPageOrientation.LANDSCAPE) //, derbyPersistanceFactory)
		val path="examples/src/main/scala/com/sysalto/report/example/fonts/roboto/"
		val fontFamily = RFontFamily(name = "Roboto",
			regular = path+"Roboto-Regular.ttf",
			bold = Some(path+"Roboto-Bold.ttf"),
			italic = Some(path+"Roboto-Italic.ttf"),
			boldItalic = Some(path+"Roboto-BoldItalic.ttf"))
		report1.setExternalFont(fontFamily)
		val font = RFont(10, fontName = "Roboto", externalFont = Some(fontFamily))
		report1.font = font
		report(report1)
	}

	def main(args: Array[String]): Unit = {
		// create tables and load data using hsqldb
		data.MutualFundsInitData.initDb()
		runReport()
	}
}
