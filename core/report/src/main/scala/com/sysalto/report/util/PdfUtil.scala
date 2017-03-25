/*
 *  This file is part of the ReactiveReports project.
 *  Copyright (c) 2017 Sysalto Corporation.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License version 3
 * as published by the Free Software Foundation with the addition of the
 * following permission added to Section 15 as permitted in Section 7(a):
 * FOR ANY PART OF THE COVERED WORK IN WHICH THE COPYRIGHT IS OWNED BY
 * Sysalto. Sysalto DISCLAIMS THE WARRANTY OF NON INFRINGEMENT
 * OF THIRD PARTY RIGHTS.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see https://www.gnu.org/licenses/agpl-3.0.en.html.
 */

package com.sysalto.report.util

import com.sysalto.report.ReportTypes._
import com.sysalto.report.reportTypes.{LineDashType, RColor, RText}
import com.sysalto.report.{WrapAllign, WrapOptions}

import scala.collection.mutable.ListBuffer


abstract class PdfUtil() {
  var name = ""

  def open(name: String)

  def newPage()

  def setFontSize(size: Int)

  def text(txt: RText, x1: Float, y1: Float, x2: Float = Float.MaxValue, y2: Float = Float.MaxValue): Unit

  def textAlignedAtPosition(txt: RText, x: Float, y: Float, index: Int): Unit

  def line(x1: Float, y1: Float, x2: Float, y2: Float, lineWidth: Float, color: RColor, lineDashType: Option[LineDashType])

  def rectangle(x1: Float, y1: Float, x2: Float, y2: Float, radius: Float = 0, color: Option[RColor], fillColor: Option[RColor])

  def drawPieChart(title: String, data: Map[String, Double], x0: Float, y0: Float, width: Float, height: Float)

  def drawBarChart(title: String, xLabel: String, yLabel: String,
                   data: List[(Double, String, String)], x0: Float, y0: Float, width: Float, height: Float)

  def drawImage(file: String, x: Float, y: Float, width: Float, height: Float, opacity: Float)

  def pgSize: Rectangle

  def close()

  def wrap(text: List[RText], x0: Float, y0: Float, x1: Float, y1: Float, wrapOption: WrapOptions.Value,
           wrapAllign: WrapAllign.Value, simulate: Boolean = false, startY: Option[Float] = None): Option[WrapBox]

  def verticalShade(rectangle: DRectangle, from: RColor, to: RColor)
}

