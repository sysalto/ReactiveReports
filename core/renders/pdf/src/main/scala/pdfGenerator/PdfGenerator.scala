package pdfGenerator

import com.sysalto.report.{ReportTypes, WrapAllign, WrapOptions}
import com.sysalto.report.ReportTypes._
import com.sysalto.report.reportTypes.{LineDashType, RColor, RText, ReportPageOrientation}
import com.sysalto.report.util.PdfUtil

/**
  * Created by marian on 11/30/16.
  */
class PdfGenerator extends PdfUtil() {
  override def open(name: String, orientation: ReportPageOrientation.Value): Unit = {

  }


  override def setPagesNumber(pgNbr: Long): Unit = {

  }

  override def rectangle(x1: Float, y1: Float, x2: Float, y2: Float, radius: Float, color: Option[RColor], fillColor: Option[RColor]): Unit = ???

  override def newPage(): Unit = ???

  override def setFontSize(size: Int): Unit = ???

  override def text(txt: RText, x1: Float, y1: Float, x2: Float, y2: Float): Unit = ???

  override def textAlignedAtPosition(txt: RText, x: Float, y: Float, index: Int): Unit = ???


  override def line(x1: Float, y1: Float, x2: Float, y2: Float, lineWidth: Float, color: RColor, lineDashType: Option[LineDashType]): Unit = ???

  //  override def rectangle(x1: Float, y1: Float, x2: Float, y2: Float, radius: Float, color: Option[RColor]): Unit = ???

  override def drawPieChart(title: String, data: Map[String, Double], x0: Float, y0: Float, width: Float, height: Float): Unit = ???

  override def drawBarChart(title: String, xLabel: String, yLabel: String, data: List[(Double, String, String)], x0: Float, y0: Float, width: Float, height: Float): Unit = ???

  override def drawImage(file: String, x: Float, y: Float, width: Float, height: Float, opacity: Float): Unit = ???

  override def pgSize: Rectangle = ???

  override def close(): Unit = {

  }

  override def wrap(text: List[RText], x0: Float, y0: Float, x1: Float, y1: Float, wrapOption: WrapOptions.Value,
                    wrapAllign: WrapAllign.Value, simulate: Boolean, startY: Option[Float],lineHeight:Float=0): Option[WrapBox] = ???

  override def verticalShade(rectangle: DRectangle, from: RColor, to: RColor): Unit = ???
}
