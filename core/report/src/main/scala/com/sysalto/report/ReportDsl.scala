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

import com.sysalto.report.ReportTypes._
import com.sysalto.report.reportTypes.{LineDashType, ReportCell, ReportColor, ReportTxt}

import scala.collection.mutable.ListBuffer


class RectangleDsl(report: Report) {

	private[this] var x1, y1, x2, y2, dradius = 0.0f
	private[this] var fromColor: ReportColor = _
	private[this] var toColor: ReportColor = _
	private[this] var dcolor: Option[ReportColor] = None
	private[this] var dFillColor: Option[ReportColor] = None

	/*
	start point of the rectangle
	 */
	def from(x1: Float, y1: Float): RectangleDsl = {
		this.x1 = x1
		this.y1 = y1
		this
	}

	/*
	end point of the rectangle.
	 */
	def to(x1: Float, y1: Float): RectangleDsl = {
		this.x2 = x1
		this.y2 = y1
		this
	}

	/*
	radius
	 */
	def radius(radius: Float): RectangleDsl = {
		this.dradius = radius
		this
	}

	/*
	set border color
 */
	def color(color: ReportColor): RectangleDsl = {
		this.dcolor = Some(color)
		this
	}

	/*
set fillColor color
*/
	def fillColor(color: ReportColor): RectangleDsl = {
		this.dFillColor = Some(color)
		this
	}

	/*
	fill with vertical shade
	 */
	def verticalShade(from: ReportColor, to: ReportColor): RectangleDsl = {
		this.fromColor = from
		this.toColor = to
		this
	}

	/*
	draw rectangle
	 */
	def draw(): Unit = {
		val vrectangle = DRectangle(x1, y1, x2, y2, dradius)
		if (fromColor != null) {
			report.verticalShade(vrectangle, fromColor, toColor)
		} else {
			report.drawRectangle(x1, y1, x2, y2, dradius, dcolor, dFillColor)
		}
	}

}


class TextDsl(report: Report, var rText: ReportTxt) {

	def at(x: Float): BoundaryRect = {
		report.text(this.rText, x)
		val txt=rText
		this.rText = null
		BoundaryRect(x-1,report.getYPosition-2,x+txt.txt.size*0.5f*report.lineHeight,report.getYPosition+report.lineHeight-4)
	}
}

class LineDsl(report: Report) {
	private[this] var fromX, fromY = 0f
	private[this] var toX, toY = 0f
	private[this] var lcolor: ReportColor = ReportColor(0, 0, 0)
	private[this] var lineDashType: Option[LineDashType] = None
	private[this] var lineWidth = 1f

	/*
	start point of the line
	 */
	def from(x: Float, y: Float): LineDsl = {
		fromX = x
		fromY = y
		this
	}

	/*
	end point of the line
	 */
	def to(x: Float, y: Float = -1f): LineDsl = {
		toX = x
		toY = if (y == -1) fromY else y
		this
	}

	/*
	color of the line
	 */
	def color(r: Int, g: Int, b: Int): LineDsl = {
		lcolor = ReportColor(r, g, b)
		this
	}

	def color(color: ReportColor): LineDsl = {
		lcolor = color
		this
	}

	/*
	line's type
	 */
	def lineType(lineDashType: LineDashType): LineDsl = {
		this.lineDashType = Some(lineDashType)
		this
	}

	/*
	line's width. Default -1.
	 */
	def width(lineWidth: Float): LineDsl = {
		this.lineWidth = lineWidth
		this
	}

	/*
	draws the line.
	 */
	def draw(): Unit = {
		report.line(fromX, fromY, toX, toY, lineWidth, lcolor, lineDashType)
	}

}



