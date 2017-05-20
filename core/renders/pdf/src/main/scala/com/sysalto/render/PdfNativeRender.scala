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

  override def open(name: String, orientation: ReportPageOrientation.Value): Unit = {
    new File(name).delete()
    pdfNativeGenerator = new PdfNativeGenerator(name, orientation)
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

  override def text(txt: RText, x1: Float, y1: Float, x2: Float, y2: Float): Unit = {
    pdfNativeGenerator.text(x1, y1, txt)
  }

  override def textAlignedAtPosition(txt: RText, x: Float, y: Float, index: Int): Unit = ???

  override def line(x1: Float, y1: Float, x2: Float, y2: Float, lineWidth: Float, color: RColor, lineDashType: Option[LineDashType]): Unit = {
    pdfNativeGenerator.line(x1, y1, x2, y2, lineWidth, color, lineDashType)
  }

  override def rectangle(x1: Float, y1: Float, x2: Float, y2: Float, radius: Float, color: Option[RColor], fillColor: Option[RColor]): Unit = ???

  override def drawPieChart(title: String, data: Map[String, Double], x0: Float, y0: Float, width: Float, height: Float): Unit = ???

  override def drawBarChart(title: String, xLabel: String, yLabel: String, data: List[(Double, String, String)], x0: Float, y0: Float, width: Float, height: Float): Unit = ???

  override def drawImage(file: String, x: Float, y: Float, width: Float, height: Float, opacity: Float): Unit = ???

  override def pgSize: ReportTypes.Rectangle = if (orientation == ReportPageOrientation.PORTRAIT) Rectangle(612, 792) else Rectangle(792, 612)

  override def close(): Unit = {
    pdfNativeGenerator.done()
    pdfNativeGenerator.close()
  }

  override def wrap(txtList: List[RText], x0: Float, y0: Float, x1: Float, y1: Float,
                    wrapOption: WrapOptions.Value, wrapAllign: WrapAllign.Value, simulate: Boolean,
                    startY: Option[Float],lineHeight:Float=0): Option[ReportTypes.WrapBox] = {
    pdfNativeGenerator.wrap(txtList, x0, y0, x1, y1, wrapOption, wrapAllign, simulate, startY,lineHeight)
  }

  override def verticalShade(rectangle: ReportTypes.DRectangle, from: RColor, to: RColor): Unit = ???
}
