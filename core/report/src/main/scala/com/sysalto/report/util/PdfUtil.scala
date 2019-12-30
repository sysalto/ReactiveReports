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


package com.sysalto.report.util

import com.sysalto.report.ReportTypes._
import com.sysalto.report.reportTypes._
import com.sysalto.report.WrapAlign

import scala.collection.mutable.ListBuffer


abstract class PdfUtil() {
	var name = ""

	def open(name: String, orientation: ReportPageOrientation.Value, pageFormat: ReportPageFormat,
					 persistenceFactory: PersistenceFactory, pdfCompression: Boolean):Unit


	def setPagesNumber(pgNbr: Long):Unit

	def newPage():Unit

	def linkToPage(boundaryRect: BoundaryRect, pageNbr: Long, left: Int, top: Int):Unit

	def linkToUrl(boundaryRect: BoundaryRect, url: String):Unit

	def text(txt: ReportTxt, x1: Float, y1: Float, x2: Float = Float.MaxValue, y2: Float = Float.MaxValue): Unit

	def textAlignedAtPosition(txt: ReportTxt, x: Float, y: Float, index: Int): Unit

	def line(x1: Float, y1: Float, x2: Float, y2: Float, lineWidth: Float, color: ReportColor, lineDashType: Option[LineDashType]):Unit

	def rectangle(x1: Float, y1: Float, x2: Float, y2: Float, radius: Float = 0, color: Option[ReportColor], fillColor: Option[ReportColor]):Unit

	def drawBarChart(title: String, xLabel: String, yLabel: String,
	                 data: List[(Double, String, String)], x0: Float, y0: Float, width: Float, height: Float):Unit

	def drawImage(file: String, x: Float, y: Float, width: Float, height: Float, opacity: Float):Unit

	def pgSize: Rectangle

	def close():Unit

	def wrap(text: List[ReportTxt], x0: Float, y0: Float, x1: Float, y1: Float,
	         wrapAlign: WrapAlign.Value, simulate: Boolean = false, lineHeight: Float = 0): Option[WrapBox]

	def verticalShade(rectangle: DRectangle, from: ReportColor, to: ReportColor):Unit

	def setExternalFont(externalFont: RFontFamily):Unit

	def getTextWidth(txt: ReportTxt): Float

	def getTextWidth(cell: ReportCell): List[Float]

	def directDrawMovePoint(x: Float, y: Float):Unit

	def directDrawLine(x: Float, y: Float):Unit

	def directDraw(code: String):Unit

	def directDrawFill(reportColor: ReportColor):Unit

	def directDrawClosePath():Unit

	def directDrawCircle(x: Float, y: Float, radius: Float):Unit

	def directDrawArc(x: Float, y: Float, radius: Float, startAngle: Float, endAngle: Float):Unit

	def directDrawStroke(reportColor: ReportColor):Unit

	def directFillStroke(fill: Boolean, stroke: Boolean):Unit

	def directDrawRectangle(x1: Float, y1: Float, x2: Float, y2: Float):Unit
}

