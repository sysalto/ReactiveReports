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

package com.sysalto.report

import scala.collection.mutable.ListBuffer


object ReportTypes {

  /*
  Keep  the result of the text wrap calculation
 initialY - initial coordinate
 currentY - current coordinate  - the Y of the last line of text
 linesWritten - number of lines that will be written
 */
  case class WrapBox(initialY: Float, currentY: Float, linesWritten: Int)

  /*
  class to keep the size of a page
   */
  case class Rectangle(width: Float, height: Float)


  /*
  Font attributes enum
   */
  object RFontAttribute extends Enumeration {
    val NORMAL, BOLD, ITALIC, BOLD_ITALIC = Value
  }

/*

 */
  case class RFont(var size: Int, var fontName: Option[String] = None, var attribute: RFontAttribute.Value = RFontAttribute.NORMAL, var color: RColor = RColor(0, 0, 0))

  case class RText(txt: String, var font: RFont = RFont(10)) {
    def size(fontSize: Int): RText = {
      font.size = fontSize
      this
    }

    def color(r: Int, g: Int, b: Int, opacity: Float = 1): RText = {
      font.color = RColor(r, g, b, opacity)
      this
    }

    def color(rColor: RColor): RText = {
      font.color = rColor
      this
    }

    def bold(): RText = {
      font.attribute = if (font.attribute == RFontAttribute.ITALIC) RFontAttribute.BOLD_ITALIC else RFontAttribute.BOLD
      this
    }

    def italic(): RText = {
      font.attribute = if (font.attribute == RFontAttribute.BOLD) RFontAttribute.BOLD_ITALIC else RFontAttribute.ITALIC
      this
    }

    def +(other: RText) = RTextList(ListBuffer(this, other))
  }

  case class RTextList(list: ListBuffer[RText]) {
    def +(other: RText): RTextList = {
      list += other
      this
    }
  }


  case class LineDashType(unit: Int, phase: Int)

  class RColorBase()

  case class RColor(r: Int, g: Int, b: Int, opacity: Float = 1) extends RColorBase

  case class RGradientColor(x0: Float, y0: Float, x1: Float, y1: Float, startColor: RColor, endColor: RColor) extends RColorBase

  case class DRectangle(x1: Float, y1: Float, x2: Float, y2: Float,
                        radius: Float = 0)

  object WrapOptions extends Enumeration {
    val LIMIT_TO_BOX, WRAP_TO_BOX, UNLIMITED = Value
  }

  object WrapAllign extends Enumeration {
    val NO_WRAP, WRAP_LEFT, WRAP_RIGHT, WRAP_CENTER, WRAP_JUSTIFIED = Value
  }


  case class RRow(cells: List[RCell]) {
    def calculate(report: Report): Float = {
      val y = report.getY
      val wrapList = cells.map(cell => {
        val result = report.wrap(cell.txt, cell.margin.left, y, cell.margin.right, Float.MaxValue, WrapOptions.LIMIT_TO_BOX, cell.allign, true)
        result.get.currentY
      })
      report.setYPosition(y)
      wrapList.reduceLeft((f1, f2) => if (f1 > f2) f1 else f2)
    }

    def print(report: Report): Unit = {
      val y = report.getY
      cells.foreach(cell => {
        report.wrap(cell.txt, cell.margin.left, y, cell.margin.right, Float.MaxValue, WrapOptions.LIMIT_TO_BOX, cell.allign)
      })
      report.setYPosition(y)
    }
  }


  /*
  holds current position (page number and vertical coordinate y)
   */
  case class ReportPosition(pageNbr: Long, y: Float)

  sealed abstract class ReportItem() {
    protected var deltaY = 0f

    private[report] def update(deltaY: Float): Unit = {
      this.deltaY = deltaY
    }

    private[report] def render(report: Report)
  }

  private[report] case class ReportPage(items: ListBuffer[ReportItem])

  /*
  draws a text at (x,y)
   */
  case class ReportText(txt: RText, x: Float, y: Float) extends ReportItem() {

    override def render(report: Report): Unit = {
      report.pdfUtil.text(txt, x, y - deltaY)
    }
  }

  /*
  draws a text allign at index at the point(x,y)
   */
  case class ReportTextAligned(rText: RText, x: Float, y: Float, index: Int) extends ReportItem() {
    override def render(report: Report): Unit = {
      report.pdfUtil.textAlignedAtPosition(rText, x, y - deltaY, index)

    }
  }

  /*
  text wrap class
   */
  case class ReportTextWrap(text: List[RText],
                            x0: Float, y0: Float, x1: Float, y1: Float, wrapOption: WrapOptions.Value,
                            wrapAllign: WrapAllign.Value, startY: Option[Float]) extends ReportItem() {
    override def render(report: Report): Unit = {
      report.reportWrap(text, x0, y0 - deltaY, x1, y1 - deltaY, wrapOption, wrapAllign, false, startY)

    }
  }

  /*
  pie chart class
   */
  case class ReportPieChart(title: String, data: Map[String, Double],
                            x0: Float, y0: Float, width: Float, height: Float) extends ReportItem() {
    override def render(report: Report): Unit = {
      report.pdfUtil.drawPieChart(title, data, x0, y0 - deltaY, width, height)
    }
  }

  /*
  bar chart class
   */
  case class ReportBarChart(title: String, xLabel: String, yLabel: String, data: List[(Double, String, String)],
                            x0: Float, y0: Float, width: Float, height: Float) extends ReportItem() {
    override def render(report: Report): Unit = {
      report.pdfUtil.drawBarChart(title, xLabel, yLabel, data, x0, y0 - deltaY, width, height)
    }
  }

  /*
  image class
   */
  case class ReportImage(file: String, x: Float, y: Float, width: Float, height: Float, opacity: Float) extends ReportItem() {
    override def render(report: Report): Unit = {
      report.pdfUtil.drawImage(file, x, y - deltaY, width, height, opacity)
    }
  }

  /*
  line class
   */
  case class ReportLine(x1: Float = 0, y1: Float = -1, x2: Float = -1, y2: Float = -1, lineWidth: Float, color: RColor, lineDashType: Option[LineDashType]) extends ReportItem() {
    override def render(report: Report): Unit = {
      report.pdfUtil.line(x1, y1 - deltaY, x2, y2 - deltaY, lineWidth, color, lineDashType)
    }
  }

  /*
  rectangle class
   */
  case class ReportRectangle(x1: Float, y1: Float, x2: Float, y2: Float,
                             radius: Float = 0, color: Option[RColor], fillColor: Option[RColor]) extends ReportItem() {
    override def render(report: Report): Unit = {
      report.pdfUtil.rectangle(x1, y1 - deltaY, x2, y2 - deltaY, radius, color, fillColor)
    }
  }

  /*
  vertical shade rectangle
   */
  case class ReportVerticalShade(rectangle: DRectangle, from: RColor, to: RColor) extends ReportItem() {
    override def render(report: Report): Unit = {
      report.pdfUtil.verticalShade(rectangle, from, to)
    }
  }


  // two classes for cut and paste (for keep band together)
  case class ReportCheckpoint(itemPos: Int, yCrt: Float)

  case class ReportCut(yCrt: Float, list: Seq[ReportItem])

}
