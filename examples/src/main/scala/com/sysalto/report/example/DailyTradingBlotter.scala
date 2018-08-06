package com.sysalto.report.example

import com.sysalto.render.PdfNativeFactory
import com.sysalto.report.Implicits.{Column, _}
import com.sysalto.report.example.data.DailyTradingBlotterData
import com.sysalto.report.example.data.DailyTradingBlotterData.{Account, Trade}
import com.sysalto.report.reportTypes.{CellAlign, GroupUtil, RFont, RFontFamily, ReportPageOrientation}
import com.sysalto.report.util.{GroupUtilTrait, PdfFactory}

object DailyTradingBlotter extends GroupUtilTrait {
	val headerFontColor = ReportColor(255, 255, 255)
	val headerColor = ReportColor(156, 76, 6)

	// draw background image as gradient
	private def drawbackgroundImage(report: Report): Unit = {
		report rectangle() from(0, 0) to(report.pageLayout.width, report.pageLayout.height) verticalShade(ReportColor(255, 255, 255), ReportColor(255, 255, 180)) draw()
	}

	private def setupReport(report: Report): Unit = {
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
	}

	private def reportHeader(report: Report): Unit = {
		report.nextLine(2)
		report print (ReportCell("Daily Trading Blotter" size 15 bold()) centerAlign() inside ReportMargin(0, report.pageLayout.width - 10))
		report.nextLine(2)
	}


	private def printTranHeader(report: Report, hrow: List[com.sysalto.report.reportTypes.ReportCell]): Unit = {
		val y2 = report.calculate(hrow)
		val top = report.getY - report.lineHeight
		val bottom = y2 + 2
		report rectangle() from(9, top) radius (3) to(report.pageLayout.width - 9, bottom) fillColor headerColor draw()
		report.print(hrow, CellAlign.CENTER, top, bottom)
		report.setYPosition(y2)
		report.nextLine
	}


	private def printAccounts(report: Report, accountList: Seq[Account]): Unit = {
		val deltaX = 150
		val deltaY = 50
		var crtX = 9
		var crtY = report.getY
		accountList.foreach(account => {
			if (crtX > report.pageLayout.width * 0.6) {
				crtX = 9
				crtY += (deltaY + 10)
				if (report.lineLeft < 5) {
					report.nextPage()
					report.nextLine(2)
					crtY = report.getY
				}
			}
			report.setYPosition(crtY)
			report print "Account Name".toString at(crtX, crtY)
			report print account.name at(crtX + 100, crtY)

			report.nextLine()
			report print "Plan Type".toString at (crtX)
			report print account.planType at (crtX + 100)

			report.nextLine()
			report print "Occupation".toString at (crtX)
			report print account.occupation at (crtX + 100)

			crtX += (deltaX + 10)

		})
	}


	private def printTrades(report: Report, accountList: Seq[Trade]): Unit = {
		val row = ReportRow(report.pageLayout.width * 0.6f, report.pageLayout.width - 10,
			List(Column("lta", Flex(1)), Column("poa", Flex(1))))
		val h_lta = ReportCell("LTA" bold()) leftAlign() inside(row, "lta")
		val h_poa = ReportCell("POA" bold()) leftAlign() inside(row, "poa")
		val hrow = List(h_lta, h_poa)
		report.print(hrow)
		report.nextLine
		accountList.foreach(trade=>{
			if (report.lineLeft < 5) {
				report.nextPage()
				report.nextLine
			}
			val v_lta = ReportCell(trade.lta) leftAlign() inside(row, "lta")
			val v_poa = ReportCell(trade.poa) leftAlign() inside(row, "poa")
			val vrow = List(v_lta, v_poa)
			report.print(vrow)
			report.nextLine
		})
	}

	private def report(report: Report): Unit = {
		setupReport(report)
		report.start()
		reportHeader(report)
		val agents = DailyTradingBlotterData.getData
		val agentsGroup = agents.toGroup
		val row = ReportRow(10, report.pageLayout.width - 10, List(Column("invCode", Flex(2)), Column("invDescription", Flex(4)),
			Column("tradeType", Flex(2)), Column("grossAmount", Flex(2)), Column("netAmmount", Flex(2)),
			Column("price", Flex(2)), Column("quantity", Flex(2)), Column("commision", Flex(2)), Column("orderStatus", Flex(1)),
			Column("risk", Flex(2))))

		val l_invCode = ReportCell("Inv. Code" bold() color headerFontColor) leftAlign() inside(row, "invCode")
		val l_invDescription = ReportCell("Investment Description" bold() color headerFontColor) leftAlign() inside(row, "invDescription")
		val l_tradeType = ReportCell("Trade Type" bold() color headerFontColor) leftAlign() inside(row, "tradeType")
		val l_grossAmount = ReportCell("Gross Amount" bold() color headerFontColor) leftAlign() inside(row, "grossAmount")
		val l_netAmmount = ReportCell("Net Amount" bold() color headerFontColor) leftAlign() inside(row, "netAmmount")
		val l_price = ReportCell("Price" bold() color headerFontColor) leftAlign() inside(row, "price")
		val l_quantity = ReportCell("Quantity" bold() color headerFontColor) leftAlign() inside(row, "quantity")
		val l_commision = ReportCell("Commision" bold() color headerFontColor) leftAlign() inside(row, "commision")
		val l_orderStatus = ReportCell("Order Status" bold() color headerFontColor) leftAlign() inside(row, "orderStatus")
		val l_risk = ReportCell("Risk" bold() color headerFontColor) rightAlign() inside(row, "risk")


		val hrow = List(l_invCode, l_invDescription, l_tradeType, l_grossAmount, l_netAmmount, l_price, l_quantity,
			l_commision, l_orderStatus, l_risk)
		agentsGroup.foreach(
			rec => {
				val crtRec = GroupUtil.getRec(rec)
				if (!GroupUtil.isFirstRecord(rec)) {
					report.nextPage()
				}
				report.nextLine(2)
				printTranHeader(report, hrow)
				report print ("Region:" + crtRec.region bold()) at 10
				report.nextLine()
				report print ("Branch:" + crtRec.branch bold()) at 10
				report.nextLine()
				report print ("Agent:" + crtRec.name bold()) at 10
				report.nextLine(2)
				crtRec.tranList.foreach(tran => {
					val v_invCode = ReportCell(tran.invCode) leftAlign() inside(row, "invCode")
					val v_invDescription = ReportCell(tran.invDescription) leftAlign() inside(row, "invDescription")
					val v_tradeType = ReportCell(tran.tradeType) leftAlign() inside(row, "tradeType")
					val v_grossAmount = ReportCell("" + tran.grossAmount) leftAlign() inside(row, "grossAmount")
					val v_netAmmount = ReportCell("" + tran.netAmmount) leftAlign() inside(row, "netAmmount")
					val v_price = ReportCell("" + tran.price) leftAlign() inside(row, "price")
					val v_quantity = ReportCell("" + tran.quantity) leftAlign() inside(row, "quantity")
					val v_commision = ReportCell("" + tran.commision) leftAlign() inside(row, "commision")
					val v_orderStatus = ReportCell(tran.orderStatus) leftAlign() inside(row, "orderStatus")
					val v_risk = ReportCell(tran.risk) rightAlign() inside(row, "risk")
					val v_row = List(v_invCode, v_invDescription, v_tradeType, v_grossAmount, v_netAmmount, v_price, v_quantity,
						v_commision, v_orderStatus, v_risk)
					val y2 = report.calculate(v_row)
					report.print(v_row)
					report line() from(10, y2 + 2) to report.pageLayout.width - 10 width 0.5f color(200, 200, 200) draw() //lineType LineDashType(2, 1) draw()
					report.setYPosition(y2)
					report.nextLine()
					if (report.lineLeft < 10) {
						report.nextPage()
						report.nextLine
						printTranHeader(report, hrow)
					}
				})
				val position1 = report.getCurrentPosition
				printAccounts(report, crtRec.accntList)
				val position2 = report.getCurrentPosition
				report.setCurrentPosition(position1)
				printTrades(report, crtRec.tradeList)
				val position3 = report.getCurrentPosition
				val position = if (position2 < position3) {
					position3
				} else {
					position2
				}
				report.setCurrentPosition(position)
			})


		report.render()
	}

	def runReport(): Unit = {
		implicit val pdfFactory: PdfFactory = new PdfNativeFactory()

		// create report with RocksDb persistence.Otherwise can use custom persistence for example derbyPersistanceFactory
		val report1 = Report("DailyTradingBlotter.pdf", ReportPageOrientation.LANDSCAPE) //, derbyPersistanceFactory)
		val path = "examples/src/main/scala/com/sysalto/report/example/fonts/roboto/"
		val fontFamily = RFontFamily(name = "Roboto",
			regular = path + "Roboto-Regular.ttf",
			bold = Some(path + "Roboto-Bold.ttf"),
			italic = Some(path + "Roboto-Italic.ttf"),
			boldItalic = Some(path + "Roboto-BoldItalic.ttf"))
		report1.setExternalFont(fontFamily)
		val font = RFont(10, fontName = "Roboto", externalFont = Some(fontFamily))
		report1.font = font
		report(report1)
	}

	def main(args: Array[String]): Unit = {
		runReport()
	}
}
