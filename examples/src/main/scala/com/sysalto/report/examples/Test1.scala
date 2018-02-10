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

import com.sysalto.render.PdfNativeFactory
import com.sysalto.report.Implicits._
import com.sysalto.report.reportTypes.{RFont, RFontFamily, ReportPageOrientation}

/**
	* Created by marian on 4/1/17.
	*/
object Test1 {

	def run(): Unit = {
		implicit val pdfFactory = new PdfNativeFactory()
		val report = Report("test2.pdf", ReportPageOrientation.LANDSCAPE,false)
		runReport(report)
	}

	def runReport(implicit report: Report): Unit = {
		val fontFamily = RFontFamily(name = "Roboto",
			regular = "/home/marian/transfer/font/Roboto-Regular.ttf",
			bold = Some("/home/marian/transfer/font/Roboto-Bold.ttf"),
			italic = Some("/home/marian/transfer/font/Roboto-Italic.ttf"),
			boldItalic = Some("/home/marian/transfer/font/Roboto-BoldItalic.ttf"))
		report.setExternalFont(fontFamily)
		report.nextLine(3)
		val size = 10
		val font = RFont(size, fontName = "Roboto", externalFont = Some(fontFamily))
//		report.font = font
		val str = "OK11"
		val txt1 = RText(str,font)
		//  val txt5=RText(str,RFont(size,fontName = "Roboto",fontFile = Some("/home/marian/transfer/font/Roboto-Regular.ttf")))
		//    val txt3=RText(str,RFont(size,fontName = "Calibri",fontFile = Some("/home/marian/transfer/font/calibri/Calibri.ttf")))
		//    val txt4=RText(str,RFont(size,fontName = "Lily",fontFile = Some("/home/marian/transfer/font/lily/LilyoftheValley.ttf")))
//		val txt2 = RText(str)

		val row = Row(10, report.pgSize.width - 10, List(Column("column1", 100), Column("column2", 100)))
		val bound1 = row.getColumnBound("column1")
		val bound2 = row.getColumnBound("column2")
		val cell1 = RCell(txt1) inside bound1
//		val cell2 = RCell(txt2) inside bound2
		val rrow = RRow(List(
			cell1))
//			cell2))
		report.print(rrow)
		report.nextLine(10)

		    report.nextLine()
//		    report print txt2 at 100
		//    report.nextLine()
		//    report print txt5 at 100
		//    report.nextLine()
		//    report print txt4 at 100

		report line() from(10, report.getY + 2) to 400 width 2f draw()
		report line() from(10, report.getY + 10) to 400 width 0.5f draw()

		report.render()
	}


	def main(args: Array[String]): Unit = {
		run()
	}

}
