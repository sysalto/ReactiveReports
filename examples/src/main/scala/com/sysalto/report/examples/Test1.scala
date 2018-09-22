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


package com.sysalto.report.examples

import com.sysalto.report.Implicits._
import com.sysalto.render.PdfDraw.DrawPoint
import com.sysalto.render.PdfNativeFactory
import com.sysalto.render.basic.PdfBasic
import com.sysalto.report.DirectDrawReport
import com.sysalto.report.reportTypes.{A5Format, LetterFormat, RFontFamily, ReportPageOrientation}


object Test1 {

	def run(): Unit = {
		implicit val pdfFactory = new PdfNativeFactory()
		val report = Report("test1.pdf", ReportPageOrientation.LANDSCAPE, LetterFormat, null, false)
		runReport(report)
	}

	def runReport(implicit report: Report): Unit = {
		val fontFamily1 = RFontFamily(name = "Roboto",
			regular = "/home/marian/transfer/font/Roboto-Regular.ttf",
			bold = Some("/home/marian/transfer/font/Roboto-Bold.ttf"),
			italic = Some("/home/marian/transfer/font/Roboto-Italic.ttf"),
			boldItalic = Some("/home/marian/transfer/font/Roboto-BoldItalic.ttf"))

		//		report.setExternalFont(fontFamily1)
		//		val font = RFont(20, fontName = "Roboto", externalFont = Some(fontFamily1))
		//		val font1 = RFont(20, fontName = "Helvetica")
		//		report.font = font

		//		report.nextLine(5)
		//		val str="test"
		//		report print (str size(20))  at 100
		//		report.nextLine()
		//		report print ReportTxt(str,font1) at 100

		//				report rectangle() from(100, 100) radius (10) to(200, 200)  color(ReportColor(156, 76, 6)) draw()

		//		report.line(100,100,200,200)
		//		report.directDrawMovePoint(100,200)
		//		report.directDrawLine(150,100)
		//		val code=PdfBasic.circle(new DrawPoint(200,200),100) //+PdfBasic.fill(new ReportColor(200,255,255))+
		//		//PdfBasic.rectangle(300,300,100,100)
		//		//report.directDraw(code)
		//		report.directDrawCircle(200,200,50)
		//		report.directDrawFill(new ReportColor(200,255,200))
		//		report.directDrawStroke(new ReportColor(255,50,255))
		//		report.directFillStroke(true,true)

		//		report.directDrawClosePath()
		//		report.directFillStroke(true,true)
		//
		//		report.roundRectangle(100, 100, 200, 200, 10)
		val directDraw = DirectDrawReport(report)
		directDraw.rectangle(10, 10, 100, 100)
		directDraw.setFillColor(ReportColor(100,200,100))
		directDraw.setStrokeColor(ReportColor(10,10,100))
		directDraw.fillStroke(true, true)

		directDraw.setFillColor(ReportColor(200,200,50))
		directDraw.roundRectangle(350,10,700,200,10)
//		directDraw.directFillStroke(true, false)

		directDraw.circle(200,300,100)
		directDraw.fillStroke(true, false)


		//				report.directDrawMovePoint(100, 100)
		//				report.directDrawLine(100, 200)
		//				report.directDrawArc(110, 200, 10, Math.PI.toFloat, (Math.PI*3/2).toFloat)
		//				report.directFillStroke(false, true)

		//		report rectangle() from(100, 100) radius (10) to(200, 200)  color(ReportColor(0, 0, 0)) draw()

		//		report rectangle() from(100, 100) to(300, 300)  verticalShade(ReportColor(255, 255, 255), ReportColor(255, 255, 180))  draw()

		report.render()

	}


	def main(args: Array[String]): Unit = {
		run()
	}

}
