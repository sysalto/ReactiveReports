package example.financialReport

import com.sysalto.render.PdfNativeFactory
import com.sysalto.report.Implicits.{Column, _}
import com.sysalto.report.reportTypes.{CellAlign, GroupUtil, RFont, RFontFamily, ReportPageOrientation}
import com.sysalto.report.util.{GroupUtilTrait, PdfFactory}
import example.financialReport.FinancialReportData.{Account, Trade}

import scala.collection.mutable.ListBuffer

object FinancialReportCh extends GroupUtilTrait {
	val headerFontColor = ReportColor(255, 255, 255)
	val headerColor = ReportColor(50, 50, 150)
	val headerTradeColor = ReportColor(50, 150, 200)
	val headerAccountColor = ReportColor(230, 230, 255)

	// draw background image as gradient
	private def drawbackgroundImage(report: Report): Unit = {
		report.rectangle().from(0, 0).to(report.pageLayout.width, report.pageLayout.height).verticalShade(ReportColor(255, 255, 255), ReportColor(230, 255, 255)).draw()
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

		report.footerFct = {
			case (pgNbr, pgMax) =>
				report.setYPosition(report.pageLayout.height - report.lineHeight * 3)
				report.line().from(10, report.getY).to(report.pageLayout.width - 10).draw()
				report.nextLine()
				report.print(ReportCell(s"Page $pgNbr of $pgMax".bold()).rightAlign().inside(ReportMargin(0, report.pageLayout.width - 10)))
		}

	}

	private def reportHeader(report: Report): Unit = {
		report.nextLine(2)
		report.print(ReportCell("Daily Trades".size(15).bold()).centerAlign().inside(ReportMargin(0, report.pageLayout.width - 10)))
		report.nextLine(2)
	}


	private def printTranHeader(report: Report, hrow: List[com.sysalto.report.reportTypes.ReportCell]): Unit = {
		val y2 = report.calculate(hrow)
		val top = report.getY - report.lineHeight
		val bottom = y2 + 2
		report.rectangle().from(9, top).radius (3).to(report.pageLayout.width - 9, bottom).fillColor(headerColor).draw()
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
				if (report.lineLeft < 10) {
					report.nextPage()
					report.nextLine(2)
					crtY = report.getY
				}
			}
			report.setYPosition(crtY)
			report print "Account".toString at(crtX, crtY)
			report print account.name at(crtX + 90, crtY)

			report.nextLine()
			report print "Type".toString at (crtX)
			report print account.planType at (crtX + 90)

			report.nextLine()
			report print "Occupation".toString at (crtX)
			report print account.occupation at (crtX + 90)

			report.rectangle().from(crtX - 2, crtY - report.lineHeight + 5).radius (5).to(crtX + deltaX + 4, crtY + deltaY - report.lineHeight - 2).fillColor(headerAccountColor).draw()

			crtX += (deltaX + 10)

		})
	}

	private def printTradeHeader(report: Report, hrow: List[com.sysalto.report.reportTypes.ReportCell]): Unit = {
		val y2 = report.calculate(hrow)
		val top = report.getY - report.lineHeight
		val bottom = y2 + 2
		report.rectangle().from(report.pageLayout.width * 0.6f + 100, top).radius (3).to(report.pageLayout.width - 9, bottom).fillColor(headerTradeColor).draw()
		report.print(hrow, CellAlign.CENTER, top, bottom)
		report.setYPosition(y2)
		report.nextLine
	}

	private def printTrades(report: Report, accountList: Seq[Trade]): Unit = {
		val row = ReportRow(report.pageLayout.width * 0.6f, report.pageLayout.width - 10,
			List(Column("lta", Flex(1)), Column("poa", Flex(1))))
		val h_lta = ReportCell("Amount".bold().color(headerFontColor)).rightAlign().inside(row, "lta")
		val h_poa = ReportCell("Fee".bold().color(headerFontColor)).rightAlign().inside(row, "poa")
		val hrow = List(h_lta, h_poa)
		report.print (ReportCell("TRADES".bold()).centerAlign().inside (ReportMargin(report.pageLayout.width * 0.6f + 100, report.pageLayout.width - 9)))
		report.nextLine(2)
		printTradeHeader(report, hrow)
		accountList.foreach(trade => {
			if (report.lineLeft < 5) {
				report.nextPage()
				report.nextLine
				printTradeHeader(report, hrow)
			}
			val v_lta = ReportCell("" + trade.amount).rightAlign().inside(row, "lta")
			val v_poa = ReportCell("" + trade.fee).rightAlign().inside(row, "poa")
			val vrow = List(v_lta, v_poa)
			report.print(vrow)
			report.nextLine
		})
	}

	private def computeSummaryPages(report: Report, summaryList: ListBuffer[(String, Long)]): Long = {
		var pageNbrs = 1L
		report.setSimulation(true)
		report.nextLine(2)
		report print "SUMMARY " at 100
		report.nextLine(2)
		summaryList.foreach(item => {
			if (report.lineLeft < 3) {
				report.nextPage()
				report.nextLine()
				pageNbrs += 1
			}
			report print item._1 at 10
			report print "" + item._2 at 200
			report.nextLine()
		})
		report.setSimulation(false)
		pageNbrs
	}

	private def runReport(report: Report): Unit = {
		val summaryList = ListBuffer[(String, Long)]()
		setupReport(report)
		report.start()
		reportHeader(report)
		val agents = FinancialReportData.getData
		val agentsGroup = agents.toGroup
		val row = ReportRow(10, report.pageLayout.width - 10, List(Column("invCode", Flex(2)), Column("invDescription", Flex(4)),
			Column("tradeType", Flex(2)), Column("grossAmount", Flex(2)), Column("netAmmount", Flex(2)),
			Column("price", Flex(2)), Column("quantity", Flex(2)), Column("commision", Flex(2)), Column("orderStatus", Flex(2)),
			Column("risk", Flex(2)))).setCellSpacing(10f)

		val l_invCode = ReportCell("Code".bold().color(headerFontColor)).leftAlign().inside(row, "invCode")
		val l_invDescription = ReportCell("Description".bold().color(headerFontColor)).leftAlign().inside(row, "invDescription")
		val l_tradeType = ReportCell("Trade Type".bold().color(headerFontColor)).leftAlign().inside(row, "tradeType")
		val l_grossAmount = ReportCell("Amount".bold().color(headerFontColor)).rightAlign().inside(row, "grossAmount")
		val l_netAmmount = ReportCell("Net Amount".bold().color(headerFontColor)).rightAlign().inside(row, "netAmmount")
		val l_price = ReportCell("Price".bold().color(headerFontColor)).rightAlign().inside(row, "price")
		val l_quantity = ReportCell("Quantity".bold().color(headerFontColor)).rightAlign().inside(row, "quantity")
		val l_commision = ReportCell("Commision".bold().color(headerFontColor)).rightAlign().inside(row, "commision")
		val l_orderStatus = ReportCell("Order Status".bold().color(headerFontColor)).leftAlign() inside(row, "orderStatus")
		val l_risk = ReportCell("Risk".bold().color(headerFontColor)).rightAlign().inside(row, "risk")


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
				report.print (("Region:" + crtRec.region).bold()).at(10)
				report.nextLine()
				report.print (("Branch Number:" + crtRec.branch).bold()).at(10)
				report.nextLine()
				report.print(("Agent:" + crtRec.name).bold()).at(10)
				val summarylabel = "Region:" + crtRec.region + " Branch Number:" + crtRec.branch + " Agent:" + crtRec.name
				summaryList += (summarylabel -> report.getCrtPageNbr())
				report.nextLine(2)
				crtRec.tranList.zipWithIndex.foreach { case (tran, index) => {
					val v_invCode = ReportCell("" + tran.invCode).leftAlign().inside(row, "invCode")
					val v_invDescription = ReportCell(tran.invDescription).leftAlign().inside(row, "invDescription")
					val v_tradeType = ReportCell(tran.tradeType).leftAlign().inside(row, "tradeType")
					val v_grossAmount = ReportCell("" + tran.grossAmount).rightAlign().inside(row, "grossAmount")
					val v_netAmmount = ReportCell("" + tran.netAmmount).rightAlign().inside(row, "netAmmount")
					val v_price = ReportCell("" + tran.price).rightAlign().inside(row, "price")
					val v_quantity = ReportCell("" + tran.quantity).rightAlign().inside(row, "quantity")
					val v_commision = ReportCell("" + tran.commision).rightAlign().inside(row, "commision")
					val v_orderStatus = ReportCell(tran.orderStatus).leftAlign().inside(row, "orderStatus")
					val v_risk = ReportCell(tran.risk).rightAlign().inside(row, "risk")
					val v_row = List(v_invCode, v_invDescription, v_tradeType, v_grossAmount, v_netAmmount, v_price, v_quantity,
						v_commision, v_orderStatus, v_risk)
					var y2 = report.calculate(v_row)
					report.print(v_row)
					if (index == crtRec.tranList.length - 1) {
						y2 += 5
						report.line().from(10, y2 + 2).to(report.pageLayout.width - 10).width(1f).color(50, 50, 50).draw()
					} else {
						report.line().from(10, y2 + 2).to(report.pageLayout.width - 10).width(0.5f).color(200, 200, 200).draw()
					}
					report.setYPosition(y2)
					report.nextLine()
					if (report.lineLeft < 10) {
						report.nextPage()
						report.nextLine
						printTranHeader(report, hrow)
					}
				}
				}
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

		val summaryPages=computeSummaryPages(report,summaryList)
		summaryList.zipWithIndex.foreach {case (key,index)=>{
			summaryList.update(index,(key._1,key._2+summaryPages))
		}}

		report.insertPages(summaryPages,1)
		report.nextLine(2)
		report.print(ReportCell("Daily Trades Summary".size(15).bold()).centerAlign().inside(ReportMargin(0, report.pageLayout.width - 10)))
		report.nextLine(2)
		summaryList.foreach(item=>{
			if (report.lineLeft<3) {
				report.nextPage()
				report.nextLine()
			}
			report print item._1 at 10

			val xLeft=report.getTextWidth(item._1)+15
			val xRight=report.pageLayout.width - 25 - report.getTextWidth(item._2.toString)

			report.line().from(xLeft,report.getY).to(xRight).width(0.5f).color(200, 200, 200).lineType(LineDashType(2, 1)).draw()
			val bound=report.print(ReportCell(""+item._2).rightAlign().inside( ReportMargin(0, report.pageLayout.width - 20)))
			report.setLinkToPage(bound,item._2, 0, 0)
			report.nextLine()
		})

		report.render()
	}

	def initReport(): Unit = {
		implicit val pdfFactory: PdfFactory = new PdfNativeFactory()

		// create report with RocksDb persistence.Otherwise can use custom persistence for example derbyPersistanceFactory
		val report1 = Report("examples/src/main/scala/example/financialReport/FinancialReport.pdf", ReportPageOrientation.LANDSCAPE) //, derbyPersistanceFactory)
		val path="examples/src/main/resources/fonts/NotoSans/"
		val fontFamily = RFontFamily(name = "NotoSans",
			regular = path + "NotoSansSC-Regular.otf",
			bold = Some(path + "NotoSansSC-Bold.otf"))
		report1.setExternalFont(fontFamily)
		val font = RFont(10, fontName = "NotoSans", externalFont = Some(fontFamily))
		report1.font = font
		runReport(report1)
	}

	def main(args: Array[String]): Unit = {
		initReport()
	}
}
