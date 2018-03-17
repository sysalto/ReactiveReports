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


package com.sysalto.render

import java.io.File

import com.sysalto.render.serialization.RenderReport
import com.sysalto.report.ReportTypes.{BoundaryRect, DRectangle, Rectangle}
import com.sysalto.report.reportTypes._
import com.sysalto.report.util.PdfUtil
import com.sysalto.report.{ReportTypes, WrapAlign}


class PdfNativeRenderNew extends PdfUtil {
	private[this] var pdfNativeGenerator: RenderReport = null
	private[this] var orientation = ReportPageOrientation.PORTRAIT
	private[this] lazy val PAGE_WIDTH = if (orientation == ReportPageOrientation.PORTRAIT) 612 else 792
	private[this] lazy val PAGE_HEIGHT = if (orientation == ReportPageOrientation.PORTRAIT) 792 else 612

	override def open(name: String, orientation: ReportPageOrientation.Value, pdfCompression: Boolean): Unit = {
		new File(name).delete()
		this.orientation = orientation
		pdfNativeGenerator = new RenderReport(name, PAGE_WIDTH, PAGE_HEIGHT, pdfCompression)
		pdfNativeGenerator.startPdf()

	}

	override def setPagesNumber(pgNbr: Long): Unit = {

	}


	override def newPage(): Unit = {
		pdfNativeGenerator.newPage()
	}


	def convertY(y: Float) = PAGE_HEIGHT - y

	override def text(txt: ReportTxt, x1: Float, y1: Float, x2: Float, y2: Float): Unit = {
		pdfNativeGenerator.text(x1, convertY(y1), txt)
	}

	override def textAlignedAtPosition(txt: ReportTxt, x: Float, y: Float, index: Int): Unit = ???

	override def line(x1: Float, y1: Float, x2: Float, y2: Float, lineWidth: Float, color: ReportColor, lineDashType: Option[LineDashType]): Unit = {
		pdfNativeGenerator.line(x1, convertY(y1), x2, convertY(y2), lineWidth, color, lineDashType)
	}

	override def rectangle(x1: Float, y1: Float, x2: Float, y2: Float,
	                       radius: Float, color: Option[ReportColor], fillColor: Option[ReportColor]): Unit = {
		pdfNativeGenerator.rectangle(x1, convertY(y1), x2, convertY(y2), radius, color, fillColor)
	}

	override def drawPieChart(font: RFont, title: String, data: List[(String, Double)], x0: Float, y0: Float, width: Float, height: Float): Unit = {
		pdfNativeGenerator.drawPieChart(font, title, data, x0, convertY(y0), width, height)
	}

	override def drawBarChart(title: String, xLabel: String, yLabel: String, data: List[(Double, String, String)], x0: Float, y0: Float, width: Float, height: Float): Unit = ???

	override def drawImage(file: String, x: Float, y: Float, width: Float, height: Float, opacity: Float): Unit = {
		pdfNativeGenerator.drawImage(file, x, convertY(y), width, height, opacity)
	}

	override def pgSize: ReportTypes.Rectangle = {
		if (orientation == ReportPageOrientation.PORTRAIT) Rectangle(612, 792) else Rectangle(792, 612)
	}

	override def close(): Unit = {
		pdfNativeGenerator.done()
		pdfNativeGenerator.close()
	}

	override def wrap(txtList: List[ReportTxt], x0: Float, y0: Float, x1: Float, y1: Float,
	                  wrapAlign: WrapAlign.Value, simulate: Boolean,
	                  lineHeight: Float = 0): Option[ReportTypes.WrapBox] = {
		pdfNativeGenerator.wrap(txtList, x0, convertY(y0), x1, convertY(y1), wrapAlign, simulate, lineHeight)
	}

	override def verticalShade(rectangle: ReportTypes.DRectangle, from: ReportColor, to: ReportColor): Unit = {
		val rectangle1 = DRectangle(rectangle.x1, convertY(rectangle.y1), rectangle.x2, convertY(rectangle.y2))
		pdfNativeGenerator.axialShade(rectangle.x1, convertY(rectangle.y1), rectangle.x1, convertY(rectangle.y2), rectangle1, from, to)
	}

	override def setExternalFont(externalFont: RFontFamily): Unit = {
		pdfNativeGenerator.setExternalFont(externalFont)
	}

	override def linkToPage(boundaryRect: BoundaryRect, pageNbr: Long, left: Int, top: Int): Unit = {
		pdfNativeGenerator.linkToPage(boundaryRect, pageNbr, left, top)
	}

	override def linkToUrl(boundaryRect: BoundaryRect, url: String): Unit = {
		pdfNativeGenerator.linkToUrl(boundaryRect, url)
	}

	override def getTextWidth(txt: ReportTxt): Float = pdfNativeGenerator.getTextWidth(txt)
	override def getTextWidth(cell: ReportCell): List[Float]=pdfNativeGenerator.getTextWidth(cell)
}
