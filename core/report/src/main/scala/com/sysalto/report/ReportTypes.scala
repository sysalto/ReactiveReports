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


package com.sysalto.report

import com.sysalto.report.reportTypes._

import scala.collection.mutable.ListBuffer


object ReportTypes {

	/*
	Keep  the result of the text wrap calculation
 initialY - initial coordinate
 currentY - current coordinate  - the Y of the last line of text
 linesWritten - number of lines that will be written
 */
	class WrapBox(val initialY: Float, val currentY: Float, val linesWritten: Int, val textHeight: Float)

	/*
	class to keep the size of a page
	 */
	class Rectangle(val width: Float, val height: Float)

	class BoundaryRect(val left: Float, val bottom: Float, val right: Float, val top: Float) {
		override def toString: String = {
			"" + left + " " + bottom + " " + right + " " + top
		}
	}


	class RColorBase()


	class RGradientColor(val x0: Float, val y0: Float, val x1: Float, val y1: Float, val startColor: ReportColor, val endColor: ReportColor) extends RColorBase

	class DRectangle(val x1: Float, val y1: Float, val x2: Float, val y2: Float, val radius: Float = 0)

	/*
	holds current position (page number and vertical coordinate y)
	 */
	class ReportPosition(val pageNbr: Long,val y: Float) {
		def <(pos1: ReportPosition): Boolean = {
			if (this.pageNbr < pos1.pageNbr) {
				return true
			}
			if (this.pageNbr > pos1.pageNbr) {
				return false
			}
			return this.y >= pos1.y
		}
	}

	sealed abstract class ReportItem() {
		var deltaY = 0f

		private[report] def update(deltaY: Float): Unit = {
			this.deltaY = deltaY
		}

		private[report] def render(report: Report)
	}

	private[report] case class ReportPage(items: ListBuffer[ReportItem])

	/*
		link to page
 */
	class ReportLinkToPage(val boundaryRect: BoundaryRect,val pageNbr: Long,val left: Int,val top: Int) extends ReportItem() {

		override def render(report: Report): Unit = {
			report.pdfUtil.linkToPage(boundaryRect, pageNbr, left, top)
		}
	}

	/*
	link to url
*/
	class ReportLinkToUrl(val boundaryRect: BoundaryRect,val url: String) extends ReportItem() {

		override def render(report: Report): Unit = {
			report.pdfUtil.linkToUrl(boundaryRect, url)
		}
	}

	/*
	draws a text at (x,y)
	 */
	class ReportText(val txt: ReportTxt,val x: Float,val y: Float) extends ReportItem() {

		override def render(report: Report): Unit = {
			report.pdfUtil.text(txt, x, y - deltaY)
		}
	}

	/*
	draws a text align at index at the point(x,y)
	 */
	class ReportTextAligned(val rText: ReportTxt,val  x: Float,val y: Float,val index: Int) extends ReportItem() {
		override def render(report: Report): Unit = {
			report.pdfUtil.textAlignedAtPosition(rText, x, y - deltaY, index)

		}
	}

	/*
	text wrap class
	 */
	case class ReportTextWrap(text: List[ReportTxt],
	                          x0: Float, y0: Float, x1: Float, y1: Float,
	                          wrapAlign: WrapAlign.Value) extends ReportItem() {
		override def render(report: Report): Unit = {
			report.reportWrap(text, x0, y0 - deltaY, x1, y1 - deltaY, wrapAlign, simulate = false)

		}
	}

	/*
	pie chart class
	 */
	case class ReportPieChart(font: RFont, title: String, data: List[(String, Double)],
	                          x0: Float, y0: Float, width: Float, height: Float) extends ReportItem() {
		override def render(report: Report): Unit = {
			report.pdfUtil.drawPieChart(font, title, data, x0, y0 - deltaY, width, height)
		}
	}

	/*
	bar chart class
	 */
	case class ReportBarChart(title: String, xLabel: String, yLabel: String, data: List[(Double, String, String)],
	                          x0: Float, y0: Float, width: Float, height: Float) extends ReportItem() {
		override def render(report: Report): Unit = {
			report.pdfUtil.drawBarChart(title, xLabel, yLabel, data, x0, y0 - deltaY, width, height)
		}
	}

	/*
	image class
	 */
	case class ReportImage(file: String, x: Float, y: Float, width: Float, height: Float, opacity: Float) extends ReportItem() {
		override def render(report: Report): Unit = {
			report.pdfUtil.drawImage(file, x, y - deltaY, width, height, opacity)
		}
	}

	/*
	line class
	 */
	case class ReportLine(x1: Float = 0, y1: Float = -1, x2: Float = -1, y2: Float = -1, lineWidth: Float, color: ReportColor, lineDashType: Option[LineDashType]) extends ReportItem() {
		override def render(report: Report): Unit = {
			report.pdfUtil.line(x1, y1 - deltaY, x2, y2 - deltaY, lineWidth, color, lineDashType)
		}
	}

	/*
	rectangle class
	 */
	class ReportRectangle(val x1: Float, val y1: Float, val x2: Float, val y2: Float,
	                      val radius: Float = 0, val color: Option[ReportColor], val fillColor: Option[ReportColor]) extends ReportItem() {
		override def render(report: Report): Unit = {
			report.pdfUtil.rectangle(x1, y1 - deltaY, x2, y2 - deltaY, radius, color, fillColor)
		}
	}

	class DrawMovePoint(x: Float, y: Float) extends ReportItem() {
		override def render(report: Report): Unit = {
			report.pdfUtil.drawMovePoint(x, y - deltaY)
		}
	}

	/*
	vertical shade rectangle
	 */
	case class ReportVerticalShade(rectangle: DRectangle, from: ReportColor, to: ReportColor) extends ReportItem() {
		override def render(report: Report): Unit = {
			report.pdfUtil.verticalShade(rectangle, from, to)
		}
	}


	// two classes for cut and paste (for keep band together)
	case class ReportCheckpoint(itemPos: Int, yCrt: Float)

	case class ReportCut(yCrt: Float, list: Seq[ReportItem])

}


object WrapAlign extends Enumeration {
	val NO_WRAP, WRAP_LEFT, WRAP_RIGHT, WRAP_CENTER, WRAP_JUSTIFIED = Value
}


/*
Font attributes enum
 */
object RFontAttribute extends Enumeration {
	val NORMAL, BOLD, ITALIC, BOLD_ITALIC = Value
}
