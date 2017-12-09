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



package com.sysalto.render

import java.io.{File, PrintWriter}

import com.sysalto.report.ReportTypes.{DRectangle, Rectangle}
import com.sysalto.report.reportTypes.{LineDashType, RColor, RText, ReportPageOrientation}
import com.sysalto.report.{ReportTypes, WrapAlign}
import com.sysalto.report.util.PdfUtil

/**
  * Created by marian on 4/1/17.
  */
class PdfNativeRender extends PdfUtil {
  private[this] var pdfNativeGenerator: PdfNativeGenerator = null
  private[this] var orientation = ReportPageOrientation.PORTRAIT
  private[this] lazy val PAGE_WIDTH = if (orientation == ReportPageOrientation.PORTRAIT) 612 else 792
  private[this] lazy val PAGE_HEIGHT = if (orientation == ReportPageOrientation.PORTRAIT) 792 else 612

  override def open(name: String, orientation: ReportPageOrientation.Value): Unit = {
    new File(name).delete()
    this.orientation = orientation
    pdfNativeGenerator = new PdfNativeGenerator(name,PAGE_WIDTH,PAGE_HEIGHT)
    pdfNativeGenerator.startPdf()

  }

  override def setPagesNumber(pgNbr: Long): Unit = {

  }

  override def newPage(): Unit = {
    pdfNativeGenerator.newPage()
  }


  def convertY(y:Float)=PAGE_HEIGHT-y

  override def text(txt: RText, x1: Float, y1: Float, x2: Float, y2: Float): Unit = {
    pdfNativeGenerator.text(x1, convertY(y1), txt)
  }

  override def textAlignedAtPosition(txt: RText, x: Float, y: Float, index: Int): Unit = ???

  override def line(x1: Float, y1: Float, x2: Float, y2: Float, lineWidth: Float, color: RColor, lineDashType: Option[LineDashType]): Unit = {
    pdfNativeGenerator.line(x1, convertY(y1), x2, convertY(y2), lineWidth, color, lineDashType)
  }

  override def rectangle(x1: Float, y1: Float, x2: Float, y2: Float,
                         radius: Float, color: Option[RColor], fillColor: Option[RColor]): Unit = {
    pdfNativeGenerator.rectangle(x1, convertY(y1), x2, convertY(y2), radius, color, fillColor)
  }

  override def drawPieChart(title: String, data: List[(String, Double)], x0: Float, y0: Float, width: Float, height: Float): Unit = {
    pdfNativeGenerator.drawPieChart(title, data, x0, convertY(y0), width, height)
  }

  override def drawBarChart(title: String, xLabel: String, yLabel: String, data: List[(Double, String, String)], x0: Float, y0: Float, width: Float, height: Float): Unit = ???

  override def drawImage(file: String, x: Float, y: Float, width: Float, height: Float, opacity: Float): Unit = {
    pdfNativeGenerator.drawImage(file, x, convertY(y), width, height, opacity)
  }

  override def pgSize: ReportTypes.Rectangle = {
    if (orientation == ReportPageOrientation.PORTRAIT) Rectangle(612, 792) else Rectangle(792, 612)
  }

  override def close(): Unit = {
    pdfNativeGenerator.done()
    pdfNativeGenerator.close()
  }

  override def wrap(txtList: List[RText], x0: Float, y0: Float, x1: Float, y1: Float,
                    wrapAlign: WrapAlign.Value, simulate: Boolean,
                    startY: Option[Float], lineHeight: Float = 0): Option[ReportTypes.WrapBox] = {
    pdfNativeGenerator.wrap(txtList, x0, convertY(y0), x1, convertY(y1), wrapAlign, simulate, startY, lineHeight)
  }

  override def verticalShade(rectangle: ReportTypes.DRectangle, from: RColor, to: RColor): Unit = {
    val rectangle1=DRectangle(rectangle.x1,convertY(rectangle.y1),rectangle.x2,convertY(rectangle.y2))
    pdfNativeGenerator.axialShade(rectangle.x1,convertY(rectangle.y1),rectangle.x1,convertY(rectangle.y2),rectangle1, from, to)
  }

}
