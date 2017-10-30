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

/**
  * Created by marian on 4/1/17.
  */
object Test1  {

  def run(): Unit = {
    implicit val pdfFactory = new PdfNativeFactory()
    val report = Report("Test.pdf" ,ReportPageOrientation.LANDSCAPE)
    runReport(report)
  }

  def runReport(report: Report): Unit = {
    report.nextLine(3)
    report print "A" at 100


    report.render()
  }


  def main(args: Array[String]): Unit = {
    run()
  }

}
