package com.sysalto.render

import java.io.{File, PrintWriter}

import com.sysalto.report.ReportTypes.Rectangle
import com.sysalto.report.reportTypes.{LineDashType, RColor, RText, ReportPageOrientation}
import com.sysalto.report.{ReportTypes, WrapAllign, WrapOptions}
import com.sysalto.report.util.PdfUtil

/**
  * Created by marian on 4/1/17.
  */
class PdfNativeRender extends PdfUtil {
  var pdfNativeGenerator: PdfNativeGenerator = null
  var orientation = ReportPageOrientation.PORTRAIT
  lazy val PAGE_WIDTH = if (orientation == ReportPageOrientation.PORTRAIT) 612 else 792
  lazy val PAGE_HEIGHT = if (orientation == ReportPageOrientation.PORTRAIT) 792 else 612

  override def open(name: String, orientation: ReportPageOrientation.Value): Unit = {
    new File(name).delete()
    pdfNativeGenerator = new PdfNativeGenerator(name,PAGE_WIDTH,PAGE_HEIGHT)
    pdfNativeGenerator.startPdf()
    this.orientation = orientation
  }

  override def setPagesNumber(pgNbr: Long): Unit = {

  }

  override def newPage(): Unit = {
    pdfNativeGenerator.newPage()
  }

  override def setFontSize(size: Int): Unit = {

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

  override def drawPieChart(title: String, data: Map[String, Double], x0: Float, y0: Float, width: Float, height: Float): Unit = {
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
                    wrapOption: WrapOptions.Value, wrapAllign: WrapAllign.Value, simulate: Boolean,
                    startY: Option[Float], lineHeight: Float = 0): Option[ReportTypes.WrapBox] = {
    pdfNativeGenerator.wrap(txtList, x0, convertY(y0), x1, convertY(y1), wrapOption, wrapAllign, simulate, startY, lineHeight)
  }

  override def verticalShade(rectangle: ReportTypes.DRectangle, from: RColor, to: RColor): Unit = {
    pdfNativeGenerator.axialShade(rectangle.x1,convertY(rectangle.y1),rectangle.x1,convertY(rectangle.y2),rectangle, from, to)
  }
}
