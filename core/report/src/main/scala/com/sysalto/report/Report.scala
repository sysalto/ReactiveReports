/*
 *  This file is part of the ReactiveReports project.
 *  Copyright (c) 2017 Sysalto Corporation.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License version 3
 * as published by the Free Software Foundation with the addition of the
 * following permission added to Section 15 as permitted in Section 7(a):
 * FOR ANY PART OF THE COVERED WORK IN WHICH THE COPYRIGHT IS OWNED BY
 * Sysalto. Sysalto DISCLAIMS THE WARRANTY OF NON INFRINGEMENT
 * OF THIRD PARTY RIGHTS.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see https://www.gnu.org/licenses/agpl-3.0.en.html.
 */

package com.sysalto.report


import com.sysalto.report.util.{KryoUtil, PdfFactory, RockDbUtil}

import scala.collection.mutable.ListBuffer
import ReportTypes._
import com.sysalto.report.reportTypes._
import _root_.java.util.function.{BiConsumer, Function}

import scala.collection.JavaConverters._

/**
	* Main report class
	* name - pdf file name
	*/
case class Report(name: String, val orientation: ReportPageOrientation.Value = ReportPageOrientation.PORTRAIT)(implicit pdfFactory: PdfFactory) {
	private var pageNbrs = 1L
	private var crtPageNbr = 1L
	private val crtPage = ReportPage(new ListBuffer[ReportItem]())
	private val db = RockDbUtil()
	private var fontSize = 10
	private[report] val pdfUtil = pdfFactory.getPdf


	private var crtYPosition = 0f


	var headerFct: (Report, Long, Long) => Unit = {
		case (_, _, _) =>
	}
	var footerFct: (Report, Long, Long) => Unit = {
		case (_, _, _) =>
	}
	var getHeaderSize: Long => Float = { _ => 0 }
	var getFooterSize: Long => Float = { _ => 0 }


	private def switchPages(newPage: Long): Unit = {
		if (newPage > pageNbrs + 1) {
			throw new Exception(s"$newPage > ${pageNbrs + 1}")
		}
		if (newPage == crtPageNbr) {
			return
		}
		db.write(s"page$crtPageNbr", crtPage)
		crtPage.items.clear()
		if (newPage <= pageNbrs) {
			val pageOpt = db.read[ReportPage](s"page$newPage")
			if (pageOpt.isDefined) {
				crtPage.items.appendAll(pageOpt.get.items)
			}
		}
		crtPageNbr = newPage
		if (newPage > pageNbrs) {
			pageNbrs = newPage
		}
		crtYPosition = pdfUtil.pgSize.height - getHeaderSize(newPage)
	}

	private def newPageInternal(): Unit = {
		pdfUtil.newPage()

		setFontSize(fontSize)
	}


	private[report] def reportWrap(text: List[RText], x0: Float, y0: Float, x1: Float, y1: Float,
	                               wrapOption: WrapOptions.Value, wrapAllign: WrapAllign.Value, simulate: Boolean = false,
	                               startY: Option[Float] = None): Option[WrapBox] = {
		pdfUtil.wrap(text, x0, y0, x1, y1, wrapOption, wrapAllign, simulate, startY, lineHeight)
	}

	/*
	Returns numbers of line per page
	 */
	def linesPerPage: Int = (pdfUtil.pgSize.height / lineHeight).toInt

	/*
	Returns line's height
	 */
	def lineHeight: Float = (fontSize * 1.5).toFloat

	/*
	keep page size
	 */
	lazy val pgSize: Rectangle = pdfUtil.pgSize

	/*
	Returns report's current position (page number and vertical position on page)
	 */
	def getCurrentPosition: ReportPosition = ReportPosition(crtPageNbr, crtYPosition)

	/*
	Restore a report position (page and vertical position on page)
	 */
	def setCurrentPosition(position: ReportPosition): Unit = {
		switchPages(position.pageNbr)
		crtYPosition = position.y
	}

	/*
		Convert line number to  position
	 */
	def toY(line: Int): Float = line * lineHeight

	/*
	convert position to line
	 */
	def toLine(y: Float): Int = ((pdfUtil.pgSize.height - y) / lineHeight).toInt


	/*
	set vertical position on current page
	 */
	def setYPosition(y: Float): Unit = {
		crtYPosition = pdfUtil.pgSize.height - y
	}

	/*
	set vertical position on current page to the line position
	 */
	def setCrtLine(line: Int): Unit = {
		setYPosition(line * lineHeight)
	}

	/*
	Returns the vertical position on current page - position 0 is on the top of the page
	 */
	def getY: Float = pdfUtil.pgSize.height - crtYPosition

	/*
	Create and go to the next page
	 */
	def newPage(): Unit = {
		pageNbrs += 1
		try {
			switchPages(pageNbrs)
			nextLine()
		} catch {
			case e: Throwable =>
				e.printStackTrace()
		}
	}

	/*
	go to the next lineNbr line
	 */
	def nextLine(lineNbr: Int): Unit = {
		crtYPosition = crtYPosition - lineNbr * lineHeight
	}


	/*
	Draws rectangle from (x1,y1) to (x2,y2) with optional radius and color
	x1,x2 - horizontal coordinates
	y1,y2 - vertical coordinates  with 0 starting from the top
	 */
	def drawRectangle(x1: Float, y1: Float, x2: Float, y2: Float, radius: Float = 0, color: Option[RColor] = None, fillColor: Option[RColor] = None) {
		val reportItem = ReportRectangle(x1, y1, x2, y2, radius, color, fillColor)
		crtPage.items += reportItem
	}

	/*
		Draws a line between (x1,y1) and (x2,y2) with thickness lineWidth and color
		x are horizontal coordinates and y vertical starting with 0 from the top
	 */
	def line(x1: Float = 0, y1: Float = getY, x2: Float = -1, y2: Float = -1, lineWidth: Float = 1, color: RColor = RColor(0, 0, 0),
	         lineDashType: Option[LineDashType] = None) {
		val reportItem = ReportLine(x1, y1, x2, y2, lineWidth, color, lineDashType)
		crtPage.items += reportItem
	}

	/*
		Draws txt at (x,y)
		By default y is the current y
	 */
	def text(txt: RText, x: Float, y: Float = -1): Unit = {
		val y1 = if (y == -1) getY else y
		val reportItem = ReportText(txt, x, y1)
		crtPage.items += reportItem
	}

	/*
	Draw text allign at index and position (x,y).
	By default y is current y.
	 */
	def textAligned(txt: RText, index: Int, x: Float, y: Float = -1): Unit = {
		val y1 = if (y == -1) getY else y
		val reportItem = ReportTextAligned(txt, x, y1, index)
		crtPage.items += reportItem
	}

	/*
	close the report.
	 */
	def close(): Unit = {
		pdfUtil.close()
		db.close()
	}

	/*
	Draw a pie chart with title, data from (x0,y0) with width and height dimensions.
	See jfreechart for details.
	 */
	def drawPieChart(title: String, data: List[(String, Double)], x0: Float, y0: Float, width: Float, height: Float): Unit = {
		crtPage.items += ReportPieChart(title, data, x0, y0, width, height)
	}

	def drawPieChart1(title: String, data: _root_.java.util.List[(String, Double)], x0: Float, y0: Float, width: Float, height: Float): Unit = {
		crtPage.items += ReportPieChart(title, data.asScala.toList, x0, y0, width, height)
	}

	/*
	Draw a bar chart with title,xLabel,yLabel and  data from (x0,y0) with width and height dimensions.
	See jfreechart for details.
 */
	def drawBarChart(title: String, xLabel: String, yLabel: String, data: List[(Double, String, String)], x0: Float, y0: Float, width: Float, height: Float): Unit = {
		crtPage.items += ReportBarChart(title, xLabel, yLabel, data, x0, y0, width, height)
	}

	/*
	Draw image from file at (x,y) with width and height having opacity.
	opacity is betwwen 0 (full transparent) and 1 (full opaque)
 */
	def drawImage(file: String, x: Float, y: Float, width: Float, height: Float, opacity: Float): Unit = {
		crtPage.items += ReportImage(file, x, y, width, height, opacity)
	}

	/*
	Write the text in wrap mode in a box from (x0,y0) to (x1,y1) with wrapOption and wrap allign.
	There are two types of invocations:
		simulate =true only calculate the new coordinates
		simulate=false write the text
		This is a low level function.
		Easy way is to use [RRow]
	 */
	def wrap(text: List[RText], x0: Float, y0: Float, x1: Float, y1: Float, wrapOption: WrapOptions.Value,
	         wrapAllign: WrapAllign.Value = WrapAllign.WRAP_LEFT, simulate: Boolean = false): Option[WrapBox] = {
		val text1 = text.map(item => {
			val font = item.font
			RText(item.txt, font)
		})
		if (simulate) {
			reportWrap(text1, x0, y0, x1, y1, wrapOption, wrapAllign, simulate)
		} else {
			val reportItem = ReportTextWrap(text1, x0, y0, x1, y1, wrapOption, wrapAllign, None)
			crtPage.items += reportItem
			None
		}
	}


	/*
	set default report font size
	 */
	def setFontSize(size: Int): Unit = {
		pdfUtil.setFontSize(size)
		fontSize = size
	}

	/*
		draw a rectangle with vertical shade between 'from' and 'to' colors.
	 */
	def verticalShade(rectangle: DRectangle, from: RColor, to: RColor): Unit = {
		crtPage.items += ReportVerticalShade(rectangle, from, to)
	}


	/*
	get Font size
	 */
	def getFontSize: Float = fontSize


	/*
	render the report.
	 */
	def render(): Unit = {
		db.write(s"page$crtPageNbr", crtPage)
		// call header and footer handler
		// pageNbrs=1
		for (i <- 1L to pageNbrs) {
			if (getHeaderSize(i) > 0 || getFooterSize(i) > 0) {
				val page = db.read[ReportPage](s"page$i").get
				crtPage.items.clear()
				crtPage.items.appendAll(page.items)
				if (getHeaderSize(i) > 0) {
					headerFct(this, i, pageNbrs)
				}
				if (getFooterSize(i) > 0) {
					footerFct(this, i, pageNbrs)
				}
				db.write(s"page$i", crtPage)
			}
		}
		pdfUtil.setPagesNumber(pageNbrs)
		for (i <- 1L to pageNbrs) {
			val page = db.read[ReportPage](s"page$i")
			if (page.isDefined) {
				val last = i == pageNbrs
				page.get.items.foreach(item => item.render(this))
				if (!last) {
					newPageInternal()
				}
			}
		}
	}


	/*
	number of lines left on current page.
	 */
	def lineLeft: Int = (crtYPosition / lineHeight).toInt

	/*
	print a cell (means wrapping text).
	 */
	def print(cell: RCell): Unit = {
		wrap(cell.txt, cell.margin.left, getY, cell.margin.right, Float.MaxValue,
			WrapOptions.LIMIT_TO_BOX, cell.allign)
	}

	/*
	print a RText (no wrapping).
	 */
	def print(txt: RText): TextDsl = {
		val result = new TextDsl(this, txt)
		result
	}

	/*
	function for use with report DSL to print a line.
	 */
	def line(): LineDsl = {
		new LineDsl(this)
	}

	/*
	only calculate a cell.
	 */
	def calculate(cell: RCell): WrapBox = {
		wrap(cell.txt, cell.margin.left, getY, cell.margin.right, Float.MaxValue,
			WrapOptions.LIMIT_TO_BOX, cell.allign, true).get
	}

	/*
	draw a rectamgle using dsl
	 */
	def rectangle(): RectangleDsl = {
		val reportDsl = new RectangleDsl(this)
		reportDsl
	}


	// The next three functions are for implementing keep band together

	/*
	get a checkpoint for further cut and paste.
	 */
	def checkpoint(): ReportCheckpoint = ReportCheckpoint(crtPage.items.length, getY)

	/*
	cut all the items from the previous checkpoint
	 */
	def cut(reportCheckpoint: ReportCheckpoint): ReportCut = {
		val nbr = reportCheckpoint.itemPos
		val result = crtPage.items.drop(nbr)
		val newList = crtPage.items.take(nbr)
		crtPage.items.clear()
		crtPage.items ++= newList
		ReportCut(getY, result)
	}

	/*
	paste the previous cut into current page.
	 */
	def paste(reportCheckpoint: ReportCheckpoint, reportCut: ReportCut): Unit = {
		if (reportCut.list.nonEmpty) {
			reportCut.list.foreach(item => item.update(reportCheckpoint.yCrt - getY))
			crtPage.items ++= reportCut.list
			setYPosition(getY + reportCut.yCrt - reportCheckpoint.yCrt)
		}
	}

	// java compatibility

	def getHeaderSize(fct: Function[Long, Float]) {
		getHeaderSize = { pgNbr =>
			fct.apply(pgNbr.asInstanceOf[Long])
		}
	}

	def getFooterSize(fct: Function[Long, Float]) {
		getFooterSize = { pgNbr =>
			fct.apply(pgNbr.asInstanceOf[Long])
		}
	}

	def headerFct(fct: BiConsumer[Long, Long]) {
		headerFct = {
			case (rpt, pgNbr, pgMax) =>
				fct.accept(pgNbr, pgMax)
		}
	}

	def footerFct(fct: BiConsumer[Long, Long]) {
		footerFct = {
			case (rpt, pgNbr, pgMax) =>
				fct.accept(pgNbr, pgMax)
		}
	}

	def nextLine(): Unit = {
		nextLine(1)
	}

	def text(txt: String, x: Float, y: Float): Unit = {
		text(RText(txt), x, y)
	}

	def text(txt: String, x: Float): Unit = {
		text(RText(txt), x)
	}


	def drawImage(file: String, x: Float, y: Float, width: Float, height: Float): Unit = {
		drawImage(file, x, y, width, height, 1f)
	}

	// class initialize

	pdfUtil.open(name, orientation)
	crtYPosition = pdfUtil.pgSize.height
	setFontSize(fontSize)
	KryoUtil.register(ReportText.getClass, ReportTextWrap.getClass, ReportLine.getClass, ReportRectangle.getClass)

}

object Report {
	def create(name: String, orientation: ReportPageOrientation.Value ,pdfFactory: PdfFactory): Report = {
		new Report(name,orientation)(pdfFactory)
	}
}