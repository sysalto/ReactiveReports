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
import com.sysalto.report.reportTypes.{CellAlign, RFont, RFontFamily, ReportPageOrientation}

/**
	* Created by marian on 4/1/17.
	*/
object Test1 {

	def run(): Unit = {
		implicit val pdfFactory = new PdfNativeFactory()
		val report = Report("test2.pdf", ReportPageOrientation.LANDSCAPE, false)
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
		val str = "Cell String"
		val txt1 = ReportTxt(str, font)
		//  val txt5=RText(str,RFont(size,fontName = "Roboto",fontFile = Some("/home/marian/transfer/font/Roboto-Regular.ttf")))
		//    val txt3=RText(str,RFont(size,fontName = "Calibri",fontFile = Some("/home/marian/transfer/font/calibri/Calibri.ttf")))
		//    val txt4=RText(str,RFont(size,fontName = "Lily",fontFile = Some("/home/marian/transfer/font/lily/LilyoftheValley.ttf")))
		//		val txt2 = RText(str)

//		val row = ReportRow(10, report.pgSize.width - 10, List(Column("column1", 130), Column("column2", 100),
//			Column("c3",83),Column("c4",83),Column("c5",Flex(1))))
//		val bound1 = row.getColumnBound("column2")
//		val bound2 = row.getColumnBound("column2")
//		val cell1 = ReportCell(" asaSD  \n  ASAS AaS    table des \nmatières").centerAlign() inside bound1
//		val cell2 = ReportCell(txt1).leftAlign() inside bound2
//		val rrow1 = ReportCellList(List(cell1)) //,cell2))
//		//			cell2))
//		report.print(rrow1)
//		report.nextLine(5)
		val row1 = ReportRow(10, report.pgSize.width - 10, List(Column("c1", 130), Column("c2", Flex(1))))
		val b1 = row1.getColumnBound("c1")
		val b2 = row1.getColumnBound("c2")
		report.nextLine()
		val c1b = ReportCell(" Test1 asdsadsaads  asdas asdaS D").leftAlign() inside b1
		val c2b = ReportCell(" I22XXEEAA").rightAlign() inside b2
		val rrow=List(c1b,c2b)

		val top=report.getY-15
		val bottom=report.getY+15
		report rectangle() from(2, top) radius (3) to(report.pgSize.width - 9, bottom)  draw()


		report.print(rrow,CellAlign.CENTER,top,bottom)
		val m1b=b1.left+report.getTextWidth(c1b).last
		val m2b=b2.right-report.getTextWidth(c2b.txt.head)
		val y2 = report.calculate(rrow)
		report line() from(m1b, y2 ) to (m2b) color(200, 200, 200) draw()

		report.nextLine(10)

		report.nextLine()
		//		    report print txt2 at 100
		//    report.nextLine()
		//    report print txt5 at 100
		//    report.nextLine()
		//    report print txt4 at 100

		//		report line() from(10, report.getY + 2) to 400 width 1f color(200,200,200) lineType(LineDashType(2,1))  draw()
		//		report line() from(10, report.getY + 20) to 400 width 1f lineType(LineDashType(1,1))  draw()
		//		report line() from(10, report.getY + 40) to 400 width 1f lineType(LineDashType(3,1))  draw()
		//		report line() from(10, report.getY + 60) to 400 width 1f draw()

		report.render()
	}


	def main(args: Array[String]): Unit = {
		run()
	}

}
