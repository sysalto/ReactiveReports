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

package com.sysalto.render

import java.awt.geom.Rectangle2D
import java.io.FileOutputStream

import com.itextpdf.awt.{DefaultFontMapper, PdfGraphics2D, PdfPrinterGraphics2D}
import com.itextpdf.text.Font.FontFamily
import com.itextpdf.text.pdf._
import com.itextpdf.text.{BaseColor, Chunk, Document, Element, Font, Image, PageSize}
import com.sysalto.report.{RFontAttribute, WrapAllign, WrapOptions}
import com.sysalto.report.ReportTypes._
import com.sysalto.report.reportTypes.{LineDashType, RColor, RText, ReportPageOrientation}
import com.sysalto.report.util.PdfUtil
import org.jfree.chart.ChartFactory
import org.jfree.data.category.DefaultCategoryDataset
import org.jfree.data.general.DefaultPieDataset

class PdfItextRender() extends PdfUtil() {
  var document: Document = new Document()
  private lazy val writer = PdfWriter.getInstance(document, new FileOutputStream(name))
  private val font = new Font(BaseFont.createFont)
  private lazy val pdfContent = writer.getDirectContent

  override def pgSize = Rectangle(document.getPageSize.getWidth, document.getPageSize.getHeight)


  override def setPagesNumber(pgNbr: Long): Unit = {

  }

  implicit class RColorToBaseColor(color: RColor) {
    def toBaseColor = new BaseColor(color.r, color.g, color.b)
  }

  override def open(name: String, orientation: ReportPageOrientation.Value): Unit = {
    this.name = name
    document = if (orientation == ReportPageOrientation.PORTRAIT)
      new Document()
    else
      new Document(PageSize.LETTER.rotate)
    writer
    Document.compress = false
    document
      .open()
  }

  override def setFontSize(size: Int): Unit = {
    pdfContent.setFontAndSize(font.getCalculatedBaseFont(false), size)
  }


  override def newPage(): Unit = {
    document.newPage()
  }

  override def text(rText: RText, x1: Float, y1: Float, x2: Float = Float.MaxValue, y2: Float = Float.MaxValue): Unit = {
    val ct = new ColumnText(pdfContent)
    ct.setSimpleColumn(x1, pgSize.height - y1, x2, pgSize.height - y2)
    ct.setAlignment(Element.ALIGN_LEFT)
    val font = rText.font
    val color = font.color
    val chunk = new Chunk(rText.txt, new Font(FontFamily.UNDEFINED, font.size, iTextAttribute(rText), color.toBaseColor))

    ct.addText(chunk)
    ct.setLeading(0, 1)
    ct.go(false)
  }


  override def textAlignedAtPosition(rText: RText, x: Float, y: Float, index: Int): Unit = {
    val leftText = RText(rText.txt.substring(0, index), rText.font)
    val chunk = new Chunk(leftText.txt, new Font(FontFamily.UNDEFINED, leftText.font.size, iTextAttribute(leftText)))
    val leftSize = chunk.getWidthPoint
    text(rText, x - leftSize, y)
  }

  override def line(x1: Float, y1: Float, x2: Float = -1, y2: Float = -1, lineWidth: Float,
                    color: RColor, lineDashType: Option[LineDashType]): Unit = {
    pdfContent.saveState()
    val x2v = if (x2 == -1) pgSize.width else x2
    val y2v = if (y2 == -1) y1 else y2
    if (lineDashType.isDefined) {
      pdfContent.setLineDash(lineDashType.get.unit, lineDashType.get.phase)
    }
    pdfContent.setColorStroke(color.toBaseColor)
    pdfContent.setLineWidth(lineWidth)
    pdfContent.moveTo(x1, pgSize.height - y1)
    pdfContent.lineTo(x2v, pgSize.height - y2v)
    pdfContent.stroke()
    pdfContent.restoreState()
  }

  override def rectangle(x1: Float, y1: Float, x2: Float, y2: Float, radius: Float = 0, color: Option[RColor],
                         fillColor: Option[RColor]): Unit = {
    if (color.isDefined || fillColor.isDefined) {
      pdfContent.saveState()
      if (fillColor.isDefined) {
        pdfContent.setColorFill(fillColor.get.toBaseColor)
      }
      pdfContent.setColorStroke(color.getOrElse(fillColor.get).toBaseColor)
    }
    if (radius == 0) {
      pdfContent.rectangle(x1, pgSize.height - y1, x2 - x1, y1 - y2)
    } else {
      pdfContent.roundRectangle(x1, pgSize.height - y1, x2 - x1, y1 - y2, radius)
    }
    if (color.isDefined || fillColor.isDefined) {
      val gState = new PdfGState()
      gState.setFillOpacity(color.getOrElse(fillColor.get).opacity)
      pdfContent.setGState(gState)
      pdfContent.fillStroke()
      pdfContent.restoreState()
    } else {
      pdfContent.stroke()
    }
  }


  override def close(): Unit = {
    document.close()
  }

  def iTextAttribute(rText: RText): Int = {
    rText.font.attribute match {
      case RFontAttribute.NORMAL => Font.NORMAL
      case RFontAttribute.ITALIC => Font.ITALIC
      case RFontAttribute.BOLD => Font.BOLD
      case RFontAttribute.BOLD_ITALIC => Font.BOLDITALIC
      case _ =>
        throw new Exception("No Attribute found for:" + rText)
    }
  }

  override def wrap(text: List[RText], x0: Float, y0: Float, x1: Float, y1: Float, wrapOption: WrapOptions.Value,
                    wrapAllign: WrapAllign.Value, simulate: Boolean = false,
                    startY: Option[Float] = None, lineHeight: Float = 0): Option[WrapBox] = {
    val ct = new ColumnText(pdfContent)
    ct.setSimpleColumn(x0, pgSize.height - y0, x1, pgSize.height - y1)
    ct.setAlignment(wrapAllign match {
      case WrapAllign.WRAP_LEFT => Element.ALIGN_LEFT
      case WrapAllign.WRAP_RIGHT => Element.ALIGN_RIGHT
      case WrapAllign.WRAP_CENTER => Element.ALIGN_CENTER
      case WrapAllign.WRAP_JUSTIFIED => Element.ALIGN_JUSTIFIED
      case WrapAllign.NO_WRAP =>
        Element.ALIGN_JUSTIFIED
    })
    text.foreach(item => {
      val font = item.font
      val color = font.color
      val chunk = new Chunk(item.txt, new Font(FontFamily.UNDEFINED, font.size, iTextAttribute(item), color.toBaseColor))
      ct.addText(chunk)
    })
    ct.setLeading(0, 1)
    if (startY.isDefined) {
      ct.setYLine(pgSize.height - startY.get)
    }
    val initialLine = ct.getYLine
    val status = ct.go(simulate)
    status match {
      case ColumnText.NO_MORE_TEXT => Some(WrapBox(pgSize.height - initialLine, pgSize.height - ct.getYLine, ct.getLinesWritten))
      case _ => if (wrapOption == WrapOptions.LIMIT_TO_BOX) None else Some(WrapBox(pgSize.height - initialLine, pgSize.height - ct.getYLine, ct.getLinesWritten))

    }
  }


  override def drawPieChart(title: String, data: Map[String, Double], x1: Float, y1: Float,
                            width: Float, height: Float): Unit = {
    pdfContent.saveState()
    val template = pdfContent.createTemplate(width, height)
    val graphics2d = new PdfGraphics2D(template, width, height, new DefaultFontMapper())
    val rectangle2d = new Rectangle2D.Double(0, 0, width, height)
    val dataSet = new DefaultPieDataset()
    data.foreach(item => dataSet.setValue(item._1, item._2))
    val chart = ChartFactory.createPieChart(title, dataSet, false, true, false)
    chart.getPlot.setBackgroundAlpha(0)
    chart.draw(graphics2d, rectangle2d)
    graphics2d.dispose()
    pdfContent.addTemplate(template, x1, pgSize.height - y1)
    pdfContent.restoreState()
  }

  override def drawBarChart(title: String, xLabel: String, yLabel: String,
                            data: List[(Double, String, String)], x1: Float, y1: Float,
                            width: Float, height: Float): Unit = {
    pdfContent.saveState()
    val template = pdfContent.createTemplate(width, height)
    val graphics2d = new PdfGraphics2D(template, width, height, new DefaultFontMapper())
    val rectangle2d = new Rectangle2D.Double(0, 0, width, height)
    val dataSet = new DefaultCategoryDataset()
    data.foreach(item => dataSet.setValue(item._1, item._2, item._3))
    val chart = ChartFactory.createBarChart(title, xLabel, yLabel, dataSet)
    chart.getPlot.setBackgroundAlpha(0)
    chart.draw(graphics2d, rectangle2d)
    graphics2d.dispose()
    pdfContent.addTemplate(template, x1, pgSize.height - y1)
    pdfContent.restoreState()
  }

  override def drawImage(file: String, x: Float, y: Float, width: Float, height: Float, opacity: Float): Unit = {
    val image = Image.getInstance(file)
    image.setAbsolutePosition(x, pgSize.height - y)
    image.scaleToFit(width, height)
    pdfContent.saveState()
    val state = new PdfGState()
    state.setFillOpacity(opacity)
    pdfContent.setGState(state)
    pdfContent.addImage(image)
    pdfContent.restoreState()
  }

  override def verticalShade(rectangle: DRectangle, from: RColor, to: RColor): Unit = {
    pdfContent.saveState()
    val shading = PdfShading.simpleAxial(writer, rectangle.x1, pgSize.height - rectangle.y1, rectangle.x2,
      pgSize.height - rectangle.y2, from.toBaseColor, to.toBaseColor, false, false)


    //--
    val axialPattern=new PdfShadingPattern(shading)
    pdfContent.setShadingFill(axialPattern)
    pdfContent.rectangle(rectangle.x1, pgSize.height - rectangle.y1, rectangle.x2 - rectangle.x1,
      rectangle.y1 - rectangle.y2)
pdfContent.fillStroke()
//--


    //pdfContent.paintShading(shading)
    pdfContent.restoreState()
  }
}
