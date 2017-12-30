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


package com.sysalto.report.examples

import com.sysalto.render.PdfNativeFactory
import com.sysalto.report.Implicits._
import com.sysalto.report.reportTypes.ReportPageOrientation
import com.sysalto.report.util.{PdfFactory, ResultSetUtilTrait}


object TestLink extends ResultSetUtilTrait {
	val MAX_TRAN_LENGTH = 20
	val MAX_AMMOUNT = 100000


	private def report(report: Report): Unit = {
		report.nextLine()
		report print "Ok1" at 10
//		val bound1=report print "Link1" at 10
//		report.nextLine()
//		val bound2=report print "Link2" at 10
//		report.setLink(bound1,2, 0, 0)
//		report.setLink(bound2,3, 0, 0)
//
//		report.nextPage()
//		report.nextPage()

		report.render()
	}


	def runReport(): Unit = {
		implicit val pdfFactory: PdfFactory = new PdfNativeFactory()
		val report1 = Report("Summary.pdf", ReportPageOrientation.LANDSCAPE, false)
		report(report1)
	}


	def main(args: Array[String]): Unit = {
		runReport()
	}
}
