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
import com.sysalto.report.reportTypes.{RFont, ReportPageOrientation}

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
    val str="Catelus cu parul cret fura rata din cotet el se jura ca nu fura"
    val size=12
    val txt1=RText(str,RFont(size,fontName = "Roboto",fontFile = Some("/home/marian/transfer/font/Roboto-Regular.ttf")))
  //  val txt5=RText(str,RFont(size,fontName = "Roboto",fontFile = Some("/home/marian/transfer/font/Roboto-Regular.ttf")))
//    val txt3=RText(str,RFont(size,fontName = "Calibri",fontFile = Some("/home/marian/transfer/font/calibri/Calibri.ttf")))
//    val txt4=RText(str,RFont(size,fontName = "Lily",fontFile = Some("/home/marian/transfer/font/lily/LilyoftheValley.ttf")))
    val txt2=RText(str,RFont(size))

    val row = Row(10, report.pgSize.width - 10, List(Column("column1", 100), Column("column2", 100)))
    val bound1 = row.getColumnBound("column1")
    val bound2 = row.getColumnBound("column2")
    val cell1 = RCell(txt1) inside bound1
    val cell2 = RCell(txt2) inside bound2
    val rrow = RRow(List(
     // cell1,
      cell2))
    rrow.print(report)

//    report print txt1 at 100
//    report.nextLine()
//    report print txt2 at 100
//    report.nextLine()
//    report print txt5 at 100
//    report.nextLine()
//    report print txt4 at 100

    report.render()
  }


  def main(args: Array[String]): Unit = {
    run()
  }

}
