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
import com.sysalto.report.{DirectDrawReport, ReportChart}
import com.sysalto.report.reportTypes.{ReportCell => _, ReportMargin => _, _}

import scala.collection.mutable.ListBuffer


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

				report.setExternalFont(fontFamily1)
				val font = RFont(10, fontName = "Roboto", externalFont = Some(fontFamily1))
		//		val font1 = RFont(20, fontName = "Helvetica")
//				report.font = font

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

//		val directDraw = new DirectDrawReport(report)
//		directDraw.rectangle(10, 10, 100, 100)
//		directDraw.setFillColor(ReportColor(100,200,100))
//		directDraw.setStrokeColor(ReportColor(10,10,100))
//		directDraw.fillStroke(true, true)
//
//		directDraw.setFillColor(ReportColor(200,200,50))
//		directDraw.roundRectangle(350,10,700,200,10)
//
//		directDraw.circle(200,300,100)
//		directDraw.fillStroke(true, false)
//		directDraw.circle(200,300,100)

//		directDraw.movePoint(400, 300)
//		directDraw.arc(300, 300, 100, 0, 1.5f)
//		directDraw.fillStroke(false, true)


//		val p1=getPoint(new DrawPoint(300,300),100,1.5f)
//		val p2=getPoint(new DrawPoint(300,300),100,-0.7f)
//		directDraw.movePoint(300,300)
//		directDraw.lineTo(p1.x,p1.y)
//		directDraw.arc(300, 300, 100, 1.5f,-0.7f)
////		directDraw.lineTo(p2.x, p2.y)
//		directDraw.closePath
//		directDraw.fillStroke(true, false)

		testWrap(report)
//		testChart(report)


		//				report.directDrawMovePoint(100, 100)
		//				report.directDrawLine(100, 200)
		//				report.directDrawArc(110, 200, 10, Math.PI.toFloat, (Math.PI*3/2).toFloat)
		//				report.directFillStroke(false, true)

		//		report rectangle() from(100, 100) radius (10) to(200, 200)  color(ReportColor(0, 0, 0)) draw()

		//		report rectangle() from(100, 100) to(300, 300)  verticalShade(ReportColor(255, 255, 255), ReportColor(255, 255, 180))  draw()

		report.render()

	}

	def testWrap(report:Report): Unit = {
		report.nextLine(2)
		val cell_P2 = ReportCell("The RRSP contribution deadline for the 2018 tax year is March 1, 2018. You will find your available contribution room on your most recent notice of assessment from canada Revenue Agency. " +
			"Speak to your investment representative today about maximizing your RRSP contribution room.") inside ReportMargin(10, report.pageLayout.width -10)
		report print cell_P2
	}


	def testChart(report:Report): Unit = {
		val chartData: List[(String, Double)] = List(("A",100),("B",300),("C",200),("D",500))
		val reportChart=new ReportChart(report)
//		reportChart.pieChart(report.font,"Test",chartData,20,20,400,200)
	}

	def main(args: Array[String]): Unit = {
		run()
	}

}
