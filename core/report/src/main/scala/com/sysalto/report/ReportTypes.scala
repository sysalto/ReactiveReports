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
	case class WrapBox(initialY: Float, currentY: Float, linesWritten: Int)

	/*
	class to keep the size of a page
	 */
	case class Rectangle(width: Float, height: Float)


	class RColorBase()


	case class RGradientColor(x0: Float, y0: Float, x1: Float, y1: Float, startColor: RColor, endColor: RColor) extends RColorBase

	case class DRectangle(x1: Float, y1: Float, x2: Float, y2: Float,
	                      radius: Float = 0)

	/*
	holds current position (page number and vertical coordinate y)
	 */
	case class ReportPosition(pageNbr: Long, y: Float) {
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
		protected var deltaY = 0f

		private[report] def update(deltaY: Float): Unit = {
			this.deltaY = deltaY
		}

		private[report] def render(report: Report)
	}

	private[report] case class ReportPage(items: ListBuffer[ReportItem])

	/*
		link
 */
	case class ReportLink(pageNbr:Long,left:Int,top:Int) extends ReportItem() {

		override def render(report: Report): Unit = {
			report.pdfUtil.link(pageNbr,left,top)
		}
	}

	/*
	draws a text at (x,y)
	 */
	case class ReportText(txt: RText, x: Float, y: Float) extends ReportItem() {

		override def render(report: Report): Unit = {
			report.pdfUtil.text(txt, x, y - deltaY)
		}
	}

	/*
	draws a text align at index at the point(x,y)
	 */
	case class ReportTextAligned(rText: RText, x: Float, y: Float, index: Int) extends ReportItem() {
		override def render(report: Report): Unit = {
			report.pdfUtil.textAlignedAtPosition(rText, x, y - deltaY, index)

		}
	}

	/*
	text wrap class
	 */
	case class ReportTextWrap(text: List[RText],
	                          x0: Float, y0: Float, x1: Float, y1: Float,
	                          wrapAlign: WrapAlign.Value, startY: Option[Float]) extends ReportItem() {
		override def render(report: Report): Unit = {
			report.reportWrap(text, x0, y0 - deltaY, x1, y1 - deltaY, wrapAlign, simulate = false, startY)

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
	case class ReportLine(x1: Float = 0, y1: Float = -1, x2: Float = -1, y2: Float = -1, lineWidth: Float, color: RColor, lineDashType: Option[LineDashType]) extends ReportItem() {
		override def render(report: Report): Unit = {
			report.pdfUtil.line(x1, y1 - deltaY, x2, y2 - deltaY, lineWidth, color, lineDashType)
		}
	}

	/*
	rectangle class
	 */
	case class ReportRectangle(x1: Float, y1: Float, x2: Float, y2: Float,
	                           radius: Float = 0, color: Option[RColor], fillColor: Option[RColor]) extends ReportItem() {
		override def render(report: Report): Unit = {
			report.pdfUtil.rectangle(x1, y1 - deltaY, x2, y2 - deltaY, radius, color, fillColor)
		}
	}

	/*
	vertical shade rectangle
	 */
	case class ReportVerticalShade(rectangle: DRectangle, from: RColor, to: RColor) extends ReportItem() {
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
