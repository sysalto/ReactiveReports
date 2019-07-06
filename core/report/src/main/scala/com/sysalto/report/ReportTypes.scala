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
	class ReportPosition(val pageNbr: Long, val y: Float) {
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

		private[report] def render(report: Report):Unit
	}

	private[report] class ReportPage(val items: ListBuffer[ReportItem])

	/*
		link to page
 */
	class ReportLinkToPage(val boundaryRect: BoundaryRect, val pageNbr: Long, val left: Int, val top: Int) extends ReportItem() {

		override def render(report: Report): Unit = {
			report.pdfUtil.linkToPage(boundaryRect, pageNbr, left, top)
		}
	}

	/*
	link to url
*/
	class ReportLinkToUrl(val boundaryRect: BoundaryRect, val url: String) extends ReportItem() {

		override def render(report: Report): Unit = {
			report.pdfUtil.linkToUrl(boundaryRect, url)
		}
	}

	/*
	draws a text at (x,y)
	 */
	class ReportText(val txt: ReportTxt, val x: Float, val y: Float) extends ReportItem() {

		override def render(report: Report): Unit = {
			report.pdfUtil.text(txt, x, y - deltaY)
		}
	}

	/*
	draws a text align at index at the point(x,y)
	 */
	class ReportTextAligned(val rText: ReportTxt, val x: Float, val y: Float, val index: Int) extends ReportItem() {
		override def render(report: Report): Unit = {
			report.pdfUtil.textAlignedAtPosition(rText, x, y - deltaY, index)

		}
	}

	/*
	text wrap class
	 */
	class ReportTextWrap(val text: List[ReportTxt],
	                     val x0: Float, val y0: Float, val x1: Float, val y1: Float,
	                     val wrapAlign: WrapAlign.Value) extends ReportItem() {
		override def render(report: Report): Unit = {
			report.reportWrap(text, x0, y0 - deltaY, x1, y1 - deltaY, wrapAlign, simulate = false)

		}

		assert(x1 - x0 > 0)
	}


	/*
	bar chart class
	 */
	class ReportBarChart(val title: String, val xLabel: String, val yLabel: String, val data: List[(Double, String, String)],
	                     val x0: Float, val y0: Float, val width: Float, val height: Float) extends ReportItem() {
		override def render(report: Report): Unit = {
			report.pdfUtil.drawBarChart(title, xLabel, yLabel, data, x0, y0 - deltaY, width, height)
		}
	}

	/*
	image class
	 */
	class ReportImage(val file: String, val x: Float, val y: Float, val width: Float, val height: Float, val opacity: Float) extends ReportItem() {
		override def render(report: Report): Unit = {
			report.pdfUtil.drawImage(file, x, y - deltaY, width, height, opacity)
		}
	}

	/*
	line class
	 */
	class ReportLine(val x1: Float = 0, val y1: Float = -1, val x2: Float = -1, val y2: Float = -1, val lineWidth: Float, val color: ReportColor, val lineDashType: Option[LineDashType]) extends ReportItem() {
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

	class DirectDrawMovePoint(val x: Float, val y: Float) extends ReportItem() {
		override def render(report: Report): Unit = {
			report.pdfUtil.directDrawMovePoint(x, y - deltaY)
		}
	}


	class DirectDrawLine(val x: Float, val y: Float) extends ReportItem() {
		override def render(report: Report): Unit = {
			report.pdfUtil.directDrawLine(x, y - deltaY)
		}
	}

	class DirectDraw(val code: String) extends ReportItem() {
		override def render(report: Report): Unit = {
			report.pdfUtil.directDraw(code)
		}
	}

	class DirectDrawFill(val reportColor: ReportColor) extends ReportItem() {
		override def render(report: Report): Unit = {
			report.pdfUtil.directDrawFill(reportColor)
		}
	}


	class DirectDrawClosePath() extends ReportItem() {
		override def render(report: Report): Unit = {
			report.pdfUtil.directDrawClosePath()
		}
	}

	class DirectDrawStroke(val reportColor: ReportColor) extends ReportItem() {
		override def render(report: Report): Unit = {
			report.pdfUtil.directDrawStroke(reportColor)
		}
	}

	class DirectDrawCircle(val x: Float, val y: Float, val radius: Float) extends ReportItem() {
		override def render(report: Report): Unit = {
			report.pdfUtil.directDrawCircle(x, y - deltaY, radius)
		}
	}

	class DirectDrawArc(val x: Float, val y: Float, val radius: Float, val startAngle: Float, val endAngle: Float) extends ReportItem() {
		override def render(report: Report): Unit = {
			report.pdfUtil.directDrawArc(x, y - deltaY, radius, startAngle, endAngle)
		}
	}


	class DirectFillStroke(val fill: Boolean, val stroke: Boolean) extends ReportItem() {
		override def render(report: Report): Unit = {
			report.pdfUtil.directFillStroke(fill, stroke)
		}
	}


	class DirectDrawRectangle(val x1: Float, val y1: Float, val x2: Float, val y2: Float) extends ReportItem() {
		override def render(report: Report): Unit = {
			report.pdfUtil.directDrawRectangle(x1, y1, x2, y2)
		}
	}

	/*
	vertical shade rectangle
	 */
	class ReportVerticalShade(val rectangle: DRectangle, val from: ReportColor, val to: ReportColor) extends ReportItem() {
		override def render(report: Report): Unit = {
			report.pdfUtil.verticalShade(rectangle, from, to)
		}
	}


	// two classes for cut and paste (for keep band together)
	case class ReportCheckpoint(val itemPos: Int, val yCrt: Float)

	case class ReportCut(val yCrt: Float, val list: Seq[ReportItem])

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
