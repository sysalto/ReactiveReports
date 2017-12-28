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




package com.sysalto.report.util

import com.sysalto.report.ReportTypes._
import com.sysalto.report.reportTypes._
import com.sysalto.report.WrapAlign

import scala.collection.mutable.ListBuffer


abstract class PdfUtil() {
  var name = ""

  def open(name: String,orientation: ReportPageOrientation.Value,pdfCompression:Boolean)


  def setPagesNumber(pgNbr:Long)

  def newPage()
  def link(pageNbr:Long,left:Int,top:Int)

  def text(txt: RText, x1: Float, y1: Float, x2: Float = Float.MaxValue, y2: Float = Float.MaxValue): Unit

  def textAlignedAtPosition(txt: RText, x: Float, y: Float, index: Int): Unit

  def line(x1: Float, y1: Float, x2: Float, y2: Float, lineWidth: Float, color: RColor, lineDashType: Option[LineDashType])

  def rectangle(x1: Float, y1: Float, x2: Float, y2: Float, radius: Float = 0, color: Option[RColor], fillColor: Option[RColor])

  def drawPieChart(font:RFont,title: String, data: List[(String, Double)], x0: Float, y0: Float, width: Float, height: Float)

  def drawBarChart(title: String, xLabel: String, yLabel: String,
                   data: List[(Double, String, String)], x0: Float, y0: Float, width: Float, height: Float)

  def drawImage(file: String, x: Float, y: Float, width: Float, height: Float, opacity: Float)

  def pgSize: Rectangle

  def close()

  def wrap(text: List[RText], x0: Float, y0: Float, x1: Float, y1: Float,
           wrapAlign: WrapAlign.Value, simulate: Boolean = false, startY: Option[Float] = None, lineHeight:Float=0): Option[WrapBox]

  def verticalShade(rectangle: DRectangle, from: RColor, to: RColor)

  def setExternalFont(externalFont:RFontFamily)
}

