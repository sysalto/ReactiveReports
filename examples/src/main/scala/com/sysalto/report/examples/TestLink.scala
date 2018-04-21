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
import com.sysalto.report.reportTypes.ReportPageOrientation
import com.sysalto.report.util.{GroupUtilTrait, PdfFactory}


object TestLink extends GroupUtilTrait {
	val MAX_TRAN_LENGTH = 20
	val MAX_AMMOUNT = 100000


	private def report(report: Report): Unit = {
		report.nextLine()
//		report print "Ok1" at 10
		val cell=ReportCell("Link to Yahoo" size 15 bold()) centerAlign() inside ReportMargin(0, report.pageLayout.width - 10)
		report print (cell)
		report.setLinkToPage(cell.getBoundaryRect(report),2,0,0)
		report.nextLine()
		val bound1=report print "Link1" at 10
		report.nextLine()
		val bound2=report print "Link2" at 10
		val ll=report.getYPosition
		report.setLinkToPage(bound1,2, 0, 0)
		report.setLinkToPage(bound2,3, 0, 0)

		report.nextLine()
		val bound3=report print "Link to Yahoo" at 10
		report.setLinkToUrl(bound3,"https://ca.yahoo.com/")

		report.nextPage()
		report.nextPage()

		report.render()
	}


	def runReport(): Unit = {
		implicit val pdfFactory: PdfFactory = new PdfNativeFactory()
		val report1 = Report("Summary.pdf", ReportPageOrientation.LANDSCAPE)
		report(report1)
	}


	def main(args: Array[String]): Unit = {
		runReport()
	}
}
