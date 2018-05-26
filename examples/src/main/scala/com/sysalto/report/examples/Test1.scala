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

import com.sysalto.render.PdfDraw.DrawPoint
import com.sysalto.render.PdfNativeFactory
import com.sysalto.render.basic.PdfBasic
import com.sysalto.report.Implicits._
import com.sysalto.report.reportTypes.{ReportTxt => _, _}


object Test1 {

	def run(): Unit = {
		implicit val pdfFactory = new PdfNativeFactory()
		val report = Report("test2.pdf", ReportPageOrientation.LANDSCAPE,A5Format,null,false)
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
		report.line(100,100,200,200)
		report.directDrawMovePoint(100,200)
		report.directDrawLine(150,100)
		val code=PdfBasic.circle(new DrawPoint(200,200),100) //+PdfBasic.fill(new ReportColor(200,255,255))+
		//PdfBasic.rectangle(300,300,100,100)
		//report.directDraw(code)
		report.directDrawCircle(200,200,50)
		report.directDrawFill(new ReportColor(200,255,200))
		report.directDrawStroke(new ReportColor(255,50,255))
		report.directFillStroke(true,true)
		report.directDrawMovePoint(250,300)
		report.directDrawArc(300,300,100,20,90)
		report.directDrawClosePath()
		report.directFillStroke(false,true)

//		report.roundRectangle(50,50,250,350,2)
		report.directDrawRectangle(50,50,250,350)
		report.directFillStroke(false,true)

		report.render()

	}


	def main(args: Array[String]): Unit = {
		run()
	}

}
