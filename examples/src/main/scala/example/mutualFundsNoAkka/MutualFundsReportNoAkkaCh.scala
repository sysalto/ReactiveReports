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


package example.mutualFundsNoAkka

import java.text.SimpleDateFormat
import java.util.GregorianCalendar

import com.sysalto.render.PdfNativeFactory
import com.sysalto.report.Implicits._
import com.sysalto.report.ReportChart
import com.sysalto.report.reportTypes.{CellAlign, GroupUtil, RFont, RFontFamily, ReportColor, ReportPageOrientation}
import com.sysalto.report.util._
import example.mutualFundsNoAkka

import scala.collection.mutable.ListBuffer

object MutualFundsReportNoAkkaCh extends GroupUtilTrait {
	val marginOffset=20
	val sd = new SimpleDateFormat("MMM dd yyyy")
	private val date1 = new GregorianCalendar(2013, 0, 1).getTime
	private val date2 = new GregorianCalendar(2013, 11, 31).getTime
	val headerColor = ReportColor(156, 76, 6)
	val headerFontColor = ReportColor(255, 255, 255)

	// draw background image as gradient
	private def drawbackgroundImage(report: Report): Unit = {
		report.rectangle().from(0, 0).to(report.pageLayout.width, report.pageLayout.height).verticalShade(ReportColor(255, 255, 255), ReportColor(255, 255, 180)).draw()
	}

	private def reportHeader(report: Report): Unit = {
		val rs = MutualFundsInitData.query("select * from clnt")
		rs.next()

		// toMap - helper function that transform sql.ResultSet to Map[String, AnyRef] where the key is the field name
		val record = rs.toMap
		rs.close()
		report.nextLine(3)
		report.drawImage("examples/src/main/resources/images/bank_banner.jpg", marginOffset, 60, 100, 40)

		report.print(ReportCell("投资声明".size(15).bold()).centerAlign().inside(ReportMargin(0, report.pageLayout.width - marginOffset)))
		report.nextLine()

		val str = sd.format(date1) + " 至 " + sd.format(date2)
		report.print(ReportCell(str.size(15).bold()).rightAlign().inside(ReportMargin(0, report.pageLayout.width - marginOffset)))
		report.nextLine(2)
		report.print("共同基金公司".bold()).at(marginOffset)
		report.nextLine()
		report.print("团体注册退休储蓄计划".bold()).at(marginOffset)
		report.nextLine(2)
		val y = report.getY

		report.print((record value "name").toString.bold()).at(marginOffset)
		report.nextLine()
		report.print ((record value "addr1").toString).at(marginOffset)
		report.nextLine()
		report.print((record value "addr2").toString).at(marginOffset)
		report.nextLine()
		report.print((record value "addr3").toString).at(marginOffset)
		report.setYPosition(y)
		report.print (ReportCell("收款人资料".bold()).rightAlign().inside(ReportMargin(0, report.pageLayout.width - marginOffset)))
		report.nextLine()
		report.print(ReportCell((record value "benef_name").toString).rightAlign().inside(ReportMargin(0, report.pageLayout.width - marginOffset)))
		report.nextLine(2)
	}


	private def summaryOfInvestment(report: Report): Unit = {

		report.nextLine(2)
		//    report line() from(10, report.getY) to (report.pgSize.width - 10) draw()
		val row = ReportRow(marginOffset, report.pageLayout.width - marginOffset, List(Column("", 5),Column("fund_name", 150), Column("value1", Flex(1)),
			Column("value2", Flex(1)), Column("change", Flex(1)), Column("graphic", Flex(2)), Column("", 5)))
		val fundName = row.getColumnBound("fund_name")
		val value1 = row.getColumnBound("value1")
		val value2 = row.getColumnBound("value2")
		val change = row.getColumnBound("change")
		val graphic = row.getColumnBound("graphic")
		val c_fundName = ReportCell("投资概要".bold().color(headerFontColor)).leftAlign().inside(fundName)
		val c_value1 = ReportCell("价值".bold().color(headerFontColor)).rightAlign().inside(value1)
		val c_value2 = ReportCell("价值".bold().color(headerFontColor)).rightAlign().inside(value2)
		val c_change = ReportCell("更改".bold().color(headerFontColor)).rightAlign().inside(change)
		val c_graphic = ReportCell("资产组合".bold().color(headerFontColor)).rightAlign().inside(graphic)
		val rrow = List(c_fundName, c_value1, c_value2, c_change, c_graphic)
		val y2 = report.calculate(rrow)
		val top = report.getY - report.lineHeight
		val bottom = y2 + 2
		report.rectangle().from(marginOffset, top).radius(3).to(report.pageLayout.width - marginOffset, bottom).fillColor(headerColor).draw()
		report.print(rrow, CellAlign.CENTER, top, bottom)
		report.setYPosition(y2)
		report.nextLine()


		//    report line() from(marginOffset, report.getY - report.lineHeight * 0.5f) to (report.pgSize.width - marginOffset) color(200, 200, 200) draw()
		val rs = mutualFundsNoAkka.MutualFundsInitData.query("select * from sum_investment")
		val rsGroup = rs.toGroup
		var firstChar = 'A'.asInstanceOf[Int]
		var total1 = 0f
		var total2 = 0f
		var total3 = 0f
		var firstY = 0f
		val chartData: ListBuffer[(String,ReportColor, Float)] = ListBuffer()
		val rnd = new scala.util.Random
		rsGroup.foreach(
			rec => try {
				if (GroupUtil.isFirstRecord(rec)) {
					firstY = report.getY
				}
				val crtRec = GroupUtil.getRec(rec)
				val c_fundName = ReportCell(ReportTxt(firstChar.asInstanceOf[Char].toString + " ").bold() + (crtRec value "fund_name").toString).leftAlign().inside(fundName)

				val c_value1 = ReportCell((crtRec value "value1").toString).rightAlign().inside(value1)
				val c_value2 = ReportCell((crtRec value "value2").toString).rightAlign().inside(value2)
				val val1 = (crtRec value "value1").toString
				val val2 = (crtRec value "value2").toString
				val v_change = val2.toFloat - val1.toFloat
				total1 += val1.toFloat
				total2 += val2.toFloat
				total3 += v_change
				val color=ReportColor(rnd.nextInt(255),rnd.nextInt(255),rnd.nextInt(255))
				val dataItem=(firstChar.asInstanceOf[Char].toString ,color, total2)
				chartData += dataItem
				val c_change = ReportCell(v_change.toString).rightAlign().inside(change)
				val rrow = List(c_fundName, c_value1, c_value2, c_change)
				val y2 = report.calculate(rrow)
				report.print(rrow)
				if (GroupUtil.isLastRecord(rec)) {
					report.line().from(marginOffset, report.getY + 2).to(change.right).width(1f).draw()
				} else {
					report.line().from(marginOffset, report.getY + 2).to(change.right).width(0.5f).color(200, 200, 200).draw()
				}
				firstChar += 1
				report.nextLine()
			} catch {
				case e: Throwable =>
					e.printStackTrace()
			})
		rs.close()

		val trow = List(ReportCell("总".bold()).inside(fundName),
			ReportCell(total1.toString.bold()).rightAlign().inside(value1),
			ReportCell(total2.toString.bold()).rightAlign().inside(value2),
			ReportCell(total3.toString.bold()).rightAlign().inside(change))
		report.print(trow)
		val chartHeight = report.getY - firstY

		val reportChart=new ReportChart(report)
		reportChart.pieChart(report.font,"",chartData.toList, graphic.left + 15, firstY, graphic.right - graphic.left - marginOffset, chartHeight)
	}

	private def changeAccount(report: Report): Unit = {
		report.nextLine(2)
		val row = ReportRow(marginOffset, report.pageLayout.width - marginOffset, List(Column("", 5),Column("account", 250), Column("value1", Flex(1)),
			Column("value2", Flex(1)), Column("value3", Flex(1)), Column("", 5)))
		val account = row.getColumnBound("account")
		val value1 = row.getColumnBound("value1")
		val value2 = row.getColumnBound("value2")
		val value3 = row.getColumnBound("value3")
		val accountHdr = ReportCell("账户价值变动".bold().color(headerFontColor)). leftAlign().inside(account)
		val value1Hdr = ReportCell("这一时期".bold().color(headerFontColor)).rightAlign().inside(value1)
		val value2Hdr = ReportCell("今年迄今为止".bold().color(headerFontColor)).rightAlign().inside(value2)
		val value3Hdr = ReportCell(s"以来 ${sd.format(date1)}".bold().color(headerFontColor)).rightAlign().inside(value3)
		val rrow = List(accountHdr, value1Hdr, value2Hdr, value3Hdr)
		val y2 = report.calculate(rrow)
		val top = report.getY - report.lineHeight
		val bottom = y2 + 2
		report.rectangle().from(marginOffset, top).radius (3).to(report.pageLayout.width - marginOffset, bottom).fillColor(headerColor).draw()
		report.print(rrow, CellAlign.CENTER, top, bottom)
		report.setYPosition(y2)
		report.nextLine()
		val rs = mutualFundsNoAkka.MutualFundsInitData.query("select * from tran_account")
		val rsGroup = rs.toGroup
		var total1, total2, total3 = 0f
		rsGroup.foreach(
			rec => try {
				val crtRec = GroupUtil.getRec(rec)
				val name = (crtRec value "name").toString
				val r_value1 = (crtRec value "value1").toString
				val r_value2 = (crtRec value "value2").toString
				val r_value3 = (crtRec value "value3").toString
				val c_account = ReportCell(name).leftAlign().inside(account)
				val c_value1 = ReportCell(r_value1).rightAlign().inside(value1)
				val c_value2 = ReportCell(r_value2).rightAlign().inside(value2)
				val c_value3 = ReportCell(r_value3).rightAlign().inside(value3)
				total1 += r_value1.toFloat
				total2 += r_value2.toFloat
				total3 += r_value3.toFloat

				val rrow = List(c_account, c_value1, c_value2, c_value3)
				val y2 = report.calculate(rrow)
				report.print(rrow)
				val lColor = if (GroupUtil.isLastRecord(rec)) ReportColor(0, 0, 0) else ReportColor(200, 200, 200)
				report.line().from(marginOffset, report.getY + 2). to (value3.right). width(0.5f). color (lColor).lineType(LineDashType(2, 1)). draw()
				report.nextLine()
			} catch {
				case e: Throwable =>
					e.printStackTrace()
			}
		)

		rs.close()
		val accountSum = ReportCell(s"帐户价值 ${sd.format(date2)}".bold()).leftAlign().inside(account)
		val value1Sum = ReportCell(total1.toString.bold()).rightAlign().inside(value1)
		val value2Sum = ReportCell(total2.toString.bold()).rightAlign().inside(value2)
		val value3Sum = ReportCell(total3.toString.bold()).rightAlign().inside(value3)
		val frow = List(accountSum, value1Sum, value2Sum, value3Sum)
		val y3 = report.calculate(frow)
		report.print(frow)
		report.setYPosition(y3)
		report.line().from(marginOffset, report.getY + report.lineHeight * 0.5f). to (value3.right).draw()
		report.nextLine()

	}


	private def accountPerformance(report: Report): Unit = {
		val rs = mutualFundsNoAkka.MutualFundsInitData.query("select * from account_perf")
		rs.next()
		val record = rs.toMap
		rs.close()
		val row = ReportRow(marginOffset, report.pageLayout.width - marginOffset, List(Column("", 5),Column("account_perf", 150), Column("value3m", Flex(1)),
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

		val h_accountPerf = ReportCell("帐户效果".bold().color(headerFontColor)).leftAlign().inside(accountPerf)
		val h_value3m = ReportCell("3个月 %".bold().color(headerFontColor)).rightAlign().inside(value3m)
		val h_value1y = ReportCell("1年 %". bold() color headerFontColor). rightAlign() inside value1y
		val h_value3y = ReportCell("3年 %".bold() color headerFontColor). rightAlign() inside value3y
		val h_value5y = ReportCell("5年 %". bold() color headerFontColor) .rightAlign() inside value5y
		val h_value10y = ReportCell("10年 %" .bold() color headerFontColor). rightAlign() inside value10y
		val h_annualized = ReportCell(s"自年化以来 ${sd.format(date1)} %" .bold() color headerFontColor). rightAlign() inside annualized
		val hrow = List(h_accountPerf, h_value3m, h_value1y, h_value3y, h_value5y, h_value10y, h_annualized)
		val y1 = report.calculate(hrow)
		val top = report.getY - report.lineHeight
		val bottom = y1 + 2
		report.rectangle().from(marginOffset, top).radius (3) .to(report.pageLayout.width - marginOffset, bottom). fillColor(headerColor) .draw()
		report.print(hrow, CellAlign.CENTER, top, bottom)
		report.setYPosition(y1)
		report.nextLine()

		val r_accountPerf = ReportCell("您的个人收益率"). leftAlign() inside accountPerf
		val r_value3m = ReportCell((record value "value3m").toString). rightAlign() inside value3m
		val r_value1y = ReportCell((record value "value1y").toString) .rightAlign() inside value1y
		val r_value3y = ReportCell((record value "value3y").toString) .rightAlign() inside value3y
		val r_value5y = ReportCell((record value "value5y").toString) .rightAlign() inside value5y
		val r_value10y = ReportCell((record value "value10y").toString) .rightAlign() inside value10y
		val r_annualized = ReportCell((record value "annualized").toString) .rightAlign() inside annualized

		val rrow = List(r_accountPerf, r_value3m, r_value1y, r_value3y, r_value5y, r_value10y, r_annualized)
		val y2 = report.calculate(rrow)
		report.print(rrow)
		report.setYPosition(y2)
	}

	private def disclaimer(report: Report): Unit = {
		report.nextPage()
		report.nextLine(3)
		report print (ReportCell("重要信息" .bold() size 20) at 20)
		report.nextLine(3)

		// "Information about your account" paragraph
		report print (ReportCell("有关您帐户的信息 " .bold() size 16) at 20)
		report.nextLine(2)
		val cell_P1 = ReportCell("我们会确保您的财务报表准确无误。但是，您有责任仔细检查它，如果发现此信息有误，请与我们联系。如有错误，请在本声明发布日期后的60 60天内与我们联系。") inside ReportMargin(marginOffset, report.pageLayout.width - marginOffset)
		report print cell_P1
		val box_P1 = cell_P1.calculate(report)
		report.setYPosition(box_P1.currentY + report.lineHeight)
		report.nextLine(2)

		// "Registered retirement" paragraph
		report print (ReportCell("注册退休 " .bold() size 16) at 20)
		report.nextLine(2)
		val cell_P2 = ReportCell("2018纳税年度的RRSP缴款截止日期为2018年3月1日。您可以在加拿大税务局的最新评估通知中找到可用的缴款空间。") inside ReportMargin(marginOffset, report.pageLayout.width - marginOffset)
		report print cell_P2
		val box_P2 = cell_P2.calculate(report)
		report.setYPosition(box_P2.currentY + report.lineHeight)
		if (report.lineLeft < 10) {
			report.nextPage()
		}

		// "If you live in Quebec" - paragraph
		report.nextLine(2)
		report print (ReportCell("如果您住在魁北克"  .bold() size 16) at 20)
		report.nextLine(2)
		val cell_P3 = ReportCell("金融市场补偿基金会（AMF）管理“服务业金融基金会”（金融服务补偿基金）") inside ReportMargin(marginOffset, report.pageLayout.width - marginOffset)
		report print cell_P3
	}


	private def runReport(report: Report): Unit = {

		// set page header size(height) at 50 and 0 (no page header) for the first page.
		report.setHeaderSize = { pgNbr =>
			if (pgNbr == 1) 0f else 80f
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
				report.setYPosition(30)
				val row = ReportRow(marginOffset, report.pageLayout.width - marginOffset, List(Column("column1", Flex(1)), Column("column2", Flex(1)),
					Column("column3", Flex(1))))
				val column1 = row.getColumnBound("column1")
				val column2 = row.getColumnBound("column2")
				val column3 = row.getColumnBound("column3")

				val h_column1 = ReportCell("账户类型" .bold()). leftAlign() inside column1
				val h_column2 = ReportCell("您的帐号" .bold()). leftAlign() inside column2
				val h_column3 = ReportCell("您的投资声明" .bold()) .rightAlign() inside column3
				val hrow = List(h_column1, h_column2, h_column3)
				report.print(hrow)
				report.nextLine()
				val str = sd.format(date1) + " 至 " + sd.format(date2)
				val r_column1 = ReportCell("团体注册退休储蓄计划"). leftAlign() inside column1
				val r_column2 = ReportCell("123456789") .leftAlign() inside column2
				val r_column3 = ReportCell(str) .rightAlign() inside column3
				val rrow = List(r_column1, r_column2, r_column3)
				report.print(rrow)
				report.nextLine(2)
				report .line(). from(marginOffset, report.getY). to (report.pageLayout.width - marginOffset). draw()
		}

		report.footerFct = {
			case (pgNbr, pgMax) =>
				report.setYPosition(report.pageLayout.height - report.lineHeight * 3)
				report. line() .from(marginOffset, report.getY). to (report.pageLayout.width - marginOffset). draw()
				report.nextLine()
				report print (ReportCell(s"页 $pgNbr 的 $pgMax". bold()). rightAlign() inside ReportMargin(0, report.pageLayout.width - marginOffset))
		}
		val t1 = System.currentTimeMillis()
		report.start()

		reportHeader(report)
		summaryOfInvestment(report)
		changeAccount(report)
		accountPerformance(report)
		disclaimer(report)
		report.render()

	}


	def initReport(): Unit = {
		implicit val pdfFactory: PdfFactory = new PdfNativeFactory()

		// create report with RocksDb persistence.Otherwise can use custom persistence for example derbyPersistanceFactory
		val report1 = Report("examples/src/main/scala/example/mutualFundsNoAkka/MutualFundsReportNoAkkaCh.pdf", ReportPageOrientation.LANDSCAPE) //, derbyPersistanceFactory)
		val path = "examples/src/main/scala/example/fonts/NotoSans/"
		val fontFamily = RFontFamily(name = "NotoSans",
			regular = path + "NotoSansSC-Regular.otf"
			,bold = Some(path + "NotoSansSC-Bold.otf")
			)
		report1.setExternalFont(fontFamily)
		val font = RFont(10, fontName = "NotoSans", externalFont = Some(fontFamily))
		report1.font = font
		runReport(report1)
	}

	def main(args: Array[String]): Unit = {
		// create tables and load data using hsqldb
		mutualFundsNoAkka.MutualFundsInitDataCh.initDb()
		val t1=System.currentTimeMillis()
		initReport()
		val t2=System.currentTimeMillis()
		println("Time:"+(t2-t1)*0.001)
	}
}
