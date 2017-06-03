package com.sysalto.render

import java.awt.image.BufferedImage
import java.io.{ByteArrayOutputStream, File, FileOutputStream, PrintWriter}
import java.nio.charset.Charset
import javax.imageio.ImageIO

import com.sysalto.report.ReportTypes.WrapBox
import com.sysalto.report.{RFontAttribute, ReportTypes, WrapAllign, WrapOptions}
import com.sysalto.report.reportTypes.{LineDashType, RColor, RText, ReportPageOrientation}
import pdfGenerator.PageTree
import util.FontAfmParser.{parseFont, parseGlyph}
import util.wrapper.WordWrap

import scala.collection.mutable.ListBuffer

/**
  * Created by marian on 4/1/17.
  */
class PdfNativeGenerator(name: String, val orientation: ReportPageOrientation.Value) {
  val PAGE_WIDTH = 612
  val PAGE_HEIGHT = 792

  implicit val pdfWriter = new PdfWriter(name)
  implicit val allItems = ListBuffer[PdfBaseItem]()
  implicit val glypList = parseGlyph()
  val txtList = ListBuffer[PdfTxtChuck]()
  val graphicList = ListBuffer[PdfGraphicChuck]()
  var id: Long = 0
  var fontId: Long = 0

  var catalog: PdfCatalog = null
  var fontMap = scala.collection.mutable.HashMap.empty[String, PdfFont]
  var currentPage: PdfPage = null
  val pageList = ListBuffer[PdfPage]()

  private def pdfHeader(): Unit = {
    pdfWriter <<< "%PDF-1.7"
    pdfWriter <<< "%\u00a0"
    pdfWriter <<< "%"
  }


  def close(): Unit = {
    pdfWriter.close()
  }


  def line(x1: Float, y1: Float, x2: Float, y2: Float, lineWidth: Float, color: RColor, lineDashType: Option[LineDashType]): Unit = {
    graphicList += PdfLine(x1.toLong, y1.toLong, x2.toLong, y2.toLong, lineWidth.toLong, color, lineDashType)
  }

  def rectangle(x1: Float, y1: Float, x2: Float, y2: Float,
                radius: Float, color: Option[RColor], fillColor: Option[RColor], paternColor: Option[PdfPattern] = None): Unit = {
    graphicList += PdfRectangle(x1.toLong, y1.toLong, x2.toLong, y2.toLong, paternColor)
  }

  def wrap(txtList: List[RText], x0: Float, y0: Float, x1: Float, y1: Float, wrapOption: WrapOptions.Value,
           wrapAllign: WrapAllign.Value, simulate: Boolean, startY: Option[Float], lineHeight: Float): Option[ReportTypes.WrapBox] = {
    implicit val fontMetric = parseFont("Helvetica")
    implicit val wordSeparators = List(',', '.')
    val lines = WordWrap.wordWrap(txtList, x1 - x0)
    var crtY = y0
    if (!simulate) {
      lines.foreach(line => {
        line.foreach(textPos => text(x0 + textPos.x, crtY, textPos.rtext))
        crtY += lineHeight
      })
    } else {
      crtY += lineHeight * (lines.size - 1)
    }
    Some(WrapBox(y0, crtY, lines.size))
  }

  private def coordinate(x: Float, y: Float): (Float, Float) = {
    val pageHeight = if (orientation == ReportPageOrientation.PORTRAIT) PAGE_HEIGHT else PAGE_WIDTH
    if (orientation == ReportPageOrientation.PORTRAIT) {
      (x, pageHeight - y)
    } else {
      (y, x)
    }
  }

  def axialShade(x1: Float, y1: Float, x2: Float, y2: Float, rectangle: ReportTypes.DRectangle, from: RColor, to: RColor): Unit = {
    val pageHeight = if (orientation == ReportPageOrientation.PORTRAIT) PAGE_HEIGHT else PAGE_WIDTH

    val (x2, y2) = coordinate(0, 0)
    val (x3, y3) = coordinate(0, 612)

    val colorFct = new PdfShaddingFctColor(nextId(), from, to)
    //    val pdfShadding = new PdfColorShadding(nextId(), x1, pageHeight - y1, x2, pageHeight - y2, colorFct)
    val pdfShadding = new PdfColorShadding(nextId(), x2, y2, x3, y3, colorFct)
    //    val pdfShadding = new PdfColorShadding(nextId(), 612, 0,0, 0, colorFct)

    val pattern = new PdfPattern(nextId(), "P1", pdfShadding)
    currentPage.pdfPatternList = List(pattern)
    this.rectangle(rectangle.x1, rectangle.y1, rectangle.x2, rectangle.y2, 0, None, None, Some(pattern))
  }

  def drawImage(file: String, x: Float, y: Float, width: Float, height: Float, opacity: Float): Unit = {
    //    println("drawImage not yet implemented.")
    val pdfImage = new PdfImage(nextId(), "img0", file)
    graphicList += PdfDrawImage(pdfImage, x, y, 1)
    currentPage.imageList = List(pdfImage)
  }

  def drawPieChart(title: String, data: Map[String, Double], x0: Float, y0: Float, width: Float, height: Float): Unit = {
    println("drawPieChart not yet implemented.")
  }

  def text(x: Float, y: Float, txt: RText): Unit = {
    val font = if (!fontMap.contains(txt.font.fontKeyName)) {
      val font1 = new PdfFont(nextId(), nextFontId(), txt.font.fontKeyName)
      fontMap += (txt.font.fontKeyName -> font1)
      font1
    } else fontMap(txt.font.fontKeyName)
    txtList += PdfTxtChuck(x, y, txt, font.refName)
  }

  def startPdf(): Unit = {
    pdfHeader()
    catalog = new PdfCatalog(nextId())
    //    val outline = new PdfOutline(nextId())
    //    catalog.outline = Some(outline)
    currentPage = new PdfPage(nextId(), 0, orientation)
  }

  def newPage(): Unit = {
    saveCurrentPage()
    currentPage = new PdfPage(nextId(), 0, orientation, fontMap.values.toList)
  }

  def saveCurrentPage(): Unit = {
    val text = new PdfText(txtList.toList)
    val graphic = new PdfGraphic(graphicList.toList)
    currentPage.contentPage = Some(new PdfPageContent(nextId(), currentPage, List(graphic, text)))
    currentPage.fontList = fontMap.values.toList.sortBy(font => font.refName)
    pageList += currentPage
    txtList.clear()
    graphicList.clear()
  }

  def done(): Unit = {
    saveCurrentPage()

    val pageTreeList = PageTree.generatePdfCode(pageList.toList) {
      () => nextId
    }(allItems)

    catalog.pdfPageList = Some(pageTreeList)


    allItems.foreach(item => item.write(pdfWriter))

    val xrefOffset = pdfWriter.position
    pdfWriter <<< "xref"
    pdfWriter <<< s"0 ${allItems.length + 1}"
    pdfWriter <<< "0000000000 65535 f"

    allItems.foreach(item => {
      val offset = item.offset.toString
      val offsetFrmt = "0" * (10 - offset.length) + offset
      pdfWriter <<< s"${offsetFrmt} 00000 n "
    })
    pdfWriter <<< "trailer"
    pdfWriter <<< s"  <<  /Size ${allItems.length + 1}"
    pdfWriter <<< "   /Root 1 0 R"
    pdfWriter <<< " >>"
    pdfWriter <<< "startxref"
    pdfWriter <<< xrefOffset.toString
    pdfWriter << "%%EOF"

  }


  def nextId(): Long = {
    id += 1
    id
  }

  def nextFontId(): String = {
    fontId += 1
    "F" + fontId
  }
}

abstract class PdfBaseItem(val id: Long)(implicit itemList: ListBuffer[PdfBaseItem]) {
  var offset: Long = 0

  def content: Array[Byte]

  def write(pdfWriter: PdfWriter): Unit = {
    offset = pdfWriter.position
    pdfWriter << content
  }

  itemList += this

  override def toString: String = {
    s"[${this.getClass.getTypeName}]\n" + content
  }
}

class PdfCatalog(id: Long, /* var outline: Option[PdfOutline] = None,*/ var pdfPageList: Option[PdfPageList] = None)
                (implicit itemList: ListBuffer[PdfBaseItem]) extends PdfBaseItem(id) {
  override def content: Array[Byte] = {
    s"""${id} 0 obj
       |  <<  /Type /Catalog
       |      /Pages ${pdfPageList.get.id} 0 R
       |  >>
       |endobj
     """.stripMargin.getBytes
  }
}

class PdfPageList(id: Long, parentId: Option[Long] = None, var pageList: List[Long] = List())
                 (implicit itemList: ListBuffer[PdfBaseItem]) extends PdfBaseItem(id) {
  override def content: Array[Byte] = {
    val parentIdStr = if (parentId.isDefined) s"/Parent ${parentId.get} 0 R" else ""
    val pageListStr = pageList.map(id => id + " 0 R").mkString("\n")
    s"""${id} 0 obj
       |  <<  /Type /Pages ${parentIdStr}
       |      /Kids [ ${pageListStr} ]
       |      /Count ${pageList.length}
       |  >>
       |endobj
     """.stripMargin.getBytes
  }
}

class PdfShaddingFctColor(id: Long, color1: RColor, color2: RColor)
                         (implicit itemList: ListBuffer[PdfBaseItem]) extends PdfBaseItem(id) {
  override def content: Array[Byte] = {
    val r1 = color1.r / 255f
    val g1 = color1.g / 255f
    val b1 = color1.b / 255f

    val r2 = color2.r / 255f
    val g2 = color2.g / 255f
    val b2 = color2.b / 255f

    s"""${id} 0 obj
       |  <</FunctionType 2/Domain[0 1]/C0[$r1 $g1 $b1]/C1[$r2 $g2 $b2]/N 1>>
       |  endobj
     """.stripMargin.getBytes
  }
}

class PdfColorShadding(id: Long, x0: Float, y0: Float, x1: Float, y1: Float, pdfShaddingFctColor: PdfShaddingFctColor)
                      (implicit itemList: ListBuffer[PdfBaseItem]) extends PdfBaseItem(id) {
  override def content: Array[Byte] = {
    s"""${id} 0 obj
       |  <</ShadingType 2/ColorSpace/DeviceRGB/Coords[$x0 $y0  $x1 $y1]/Function ${pdfShaddingFctColor.id} 0 R>>
       |  endobj
     """.stripMargin.getBytes
  }
}

class PdfPattern(id: Long, val name: String, pdfShadding: PdfColorShadding)
                (implicit itemList: ListBuffer[PdfBaseItem]) extends PdfBaseItem(id) {
  override def content: Array[Byte] = {
    s"""${id} 0 obj
       |  <</PatternType 2/Shading ${pdfShadding.id} 0 R/Matrix[1 0 0 1 0 0]>>
       |  endobj
     """.stripMargin.getBytes
  }
}

class ImageMeta(fileName: String) {
  val file = new File(fileName)
  val bimg: BufferedImage = ImageIO.read(file)
  val width: Int = bimg.getWidth()
  val height: Int = bimg.getHeight()
  val size: Long = file.length
  val baos = new ByteArrayOutputStream()
  ImageIO.write(bimg, "jpg", baos)
  baos.flush()
  val imageInByte: Array[Byte] = baos.toByteArray
  baos.close()
  val pixelSize: Int = bimg.getColorModel.getComponentSize(0)
}

class PdfImage(id: Long, val name: String, fileName: String)(implicit itemList: ListBuffer[PdfBaseItem]) extends PdfBaseItem(id) {
  val image = new ImageMeta(fileName)

  override def content: Array[Byte] = {
    s"""${id} 0 obj
       |  <<
       | /Type /XObject
       | /Subtype /Image
       | /Width ${image.width}
       | /Height ${image.height}
       |  /ColorSpace /DeviceRGB
       |  /BitsPerComponent ${image.pixelSize}
       |  /Length ${image.imageInByte.length}
       |  /Filter /DCTDecode
       |  >>
       |  stream
       |""".stripMargin.getBytes ++
      image.imageInByte ++
       "endstream\n endobj\n".getBytes
  }
}


case class PdfDrawImage(pdfImage: PdfImage, x: Float, y: Float, scale: Float = 1, opacity: Option[Float] = None)
                       (implicit itemList: ListBuffer[PdfBaseItem]) extends PdfGraphicChuck {
  val image = pdfImage.image
  val width = image.width * scale
  val height = image.height * scale
  //  val opacityStr = if (opacity == None) "" else s"/${opacity.get.name} gs"
  val opacityStr = ""

  def content: String =
    s"""$opacityStr
       |$width 0 0 $height ${x} ${y} cm
       |/${pdfImage.name} Do
    """.stripMargin

//  def content: String =
//    s"""$opacityStr
//       |$width 0 0 $height ${x} ${y} cm
//       |/${pdfImage.name} Do
//    """.stripMargin

}

class PdfPage(id: Long, var pdfPageListId: Long = 0, val orientation: ReportPageOrientation.Value = ReportPageOrientation.PORTRAIT,
              var fontList: List[PdfFont] = List(), var pdfPatternList: List[PdfPattern] = List(),var imageList:List[PdfImage]=List(), var contentPage: Option[PdfPageContent] = None)
             (implicit itemList: ListBuffer[PdfBaseItem]) extends PdfBaseItem(id) {
  override def content: Array[Byte] = {
    val contentStr = if (contentPage.isDefined) s"/Contents ${contentPage.get.id} 0 R" else ""
    val fontStr = "/Font<<" + fontList.map(font => s"/${font.refName} ${font.id} 0 R").mkString("") + ">>"
    val patternStr = if (pdfPatternList.isEmpty) "" else "/Pattern <<" + pdfPatternList.map(item => s"/${item.name} ${item.id} 0 R").mkString(" ") + ">>"
    val imageStr = if (imageList.isEmpty) "" else "/XObject <<" + imageList.map(item => s"/${item.name} ${item.id} 0 R").mkString(" ") + ">>"
    s"""${id} 0 obj
       |  <<  /Type /Page
       |      /Parent ${pdfPageListId} 0 R
       |      /MediaBox [ 0 0 612 792 ] ${if (orientation == ReportPageOrientation.LANDSCAPE) "/Rotate 90" else ""}
       |      ${contentStr}
       |      /Resources  << ${fontStr}
       |      ${patternStr}
       |      ${imageStr}
       |                  >>
       |  >>
       |endobj
     """.stripMargin.getBytes

    s"""${id} 0 obj
       |  <<  /Type /Page
       |      /Parent ${pdfPageListId} 0 R
       |      /MediaBox ${if (orientation == ReportPageOrientation.LANDSCAPE) "[ 0 0 792 612]" else "[ 0 0 612 792 ]"}
       |      ${contentStr}
       |      /Resources  << ${fontStr}
       |      ${patternStr}
       |      ${imageStr}
       |                  >>
       |  >>
       |endobj
     """.stripMargin.getBytes
  }
}


class PdfFont(id: Long, val refName: String, fontKeyName: String)(implicit itemList: ListBuffer[PdfBaseItem]) extends PdfBaseItem(id) {
  override def content: Array[Byte] = {
    s"""${id} 0 obj
       |  <<  /Type /Font
       |      /Subtype /Type1
       |      /BaseFont /${fontKeyName}
       |      /Encoding /WinAnsiEncoding
       |  >>
       |endobj
     """.stripMargin.getBytes
  }
}

class PdfPageContent(id: Long, pdfPage: PdfPage, pageItemList: List[PdfPageItem])
                    (implicit itemList: ListBuffer[PdfBaseItem]) extends PdfBaseItem(id) {
  override def content: Array[Byte] = {
    val portraitMatrix = "1 0 0 1 0 792 cm -1 0 0 -1 0 0 cm"
    val landscapeMatrix = "0 -1 1 0 0 0 cm"
    val matrix = ""//if (pdfPage.orientation == ReportPageOrientation.PORTRAIT) portraitMatrix else landscapeMatrix
    val itemsStr = matrix + "\n" + pageItemList.foldLeft("")((s1, s2) => s1 + "\n" + s2.content)

    s"""${id} 0 obj
       |  <<  /Length ${itemsStr.length} >>
       |      stream
       |${itemsStr}endstream
       |endobj
     """.stripMargin.getBytes
  }
}

abstract class PdfPageItem {
  def content: String
}

case class PdfTxtChuck(x: Float, y: Float, rtext: RText, fontRefName: String)

abstract class PdfGraphicChuck {
  def content: String
}

//case class PdfShading(rectangle: ReportTypes.DRectangle, from: RColor, to: RColor) extends PdfGraphicChuck {
//  override def content: String = {
//    s"""-${x1} ${y1} m
//       |-${x2} ${y2} l
//       |S
//       """.stripMargin.trim
//  }
//
//}

case class PdfLine(x1: Long, y1: Long, x2: Long, y2: Long,
                   lineWidth: Long, color: RColor, lineDashType: Option[LineDashType]) extends PdfGraphicChuck {
  override def content: String = {
    s"""-${x1} ${y1} m
       |-${x2} ${y2} l
       |S
       """.stripMargin.trim
  }

}

case class PdfRectangle(x1: Long, y1: Long, x2: Long, y2: Long, fillColor: Option[PdfPattern] = None) extends PdfGraphicChuck {
  override def content: String = {
    val paternStr = if (fillColor.isDefined) s"/Pattern cs /${fillColor.get.name} scn" else ""
    val paintStr = if (paternStr.isEmpty) "S" else "B"
    s"""${paternStr}
       |-${x1} ${y1} ${x1 - x2} ${y2 - y1} re
       |${paintStr}
       """.stripMargin.trim
  }

}


class PdfText(txtList: List[PdfTxtChuck])
  extends PdfPageItem {
  override def content: String = {
    if (txtList.isEmpty) {
      return ""
    }
    val portraitMatrix = "1 0 0 1 0 792 cm -1 0 0 -1 0 0 cm"
    val landscapeMatrix = "0 -1 1 0 0 0 cm"
    val item = txtList.head
    val firstItemTxt =
      s""" BT /${item.fontRefName} ${item.rtext.font.size} Tf
         |  -1 0 0 -1 -${item.x.toLong} ${item.y.toLong} Tm
         |        ( ${item.rtext.txt} ) Tj
       """.stripMargin

    val s2 = firstItemTxt + txtList.tail.zipWithIndex.map { case (item, i) => {
      val xRel = txtList(i + 1).x.toLong - txtList(i).x.toLong
      val yRel = txtList(i + 1).y.toLong - txtList(i).y.toLong
      s"""  /${item.fontRefName} ${item.rtext.font.size} Tf
         |  ${xRel} ${-yRel} Td
         |  ( ${item.rtext.txt} ) Tj
       """.stripMargin
    }
    }.foldLeft("")((s1, s2) => s1 + s2)

    s"""${s2}
       |      ET
       """.stripMargin.trim
  }

}

class PdfGraphic(items: List[PdfGraphicChuck]) extends PdfPageItem {
  override def content: String = {
    val str = items.map(item => {
      item.content
    }).foldLeft("")((s1, s2) => s1 + "\n" + s2)

    s"""q
       |0 0 0 RG
       |1 w
       |${str}
       |Q
 """.stripMargin
  }

}


class PdfWriter(name: String) {
  new File(name).delete()
  val writer = new  FileOutputStream(name)
  var position: Long = 0

  def <<(str: String):Unit = {
    <<(str.getBytes)
  }

  def <<<(str: String):Unit = {
    val str1 = str + "\n"
    <<(str1.getBytes)
  }

  def <<(str: Array[Byte]):Unit = {
    writer.write(str)
    position += str.length
  }

  def close(): Unit = {
    writer.flush()
    writer.close()
  }
}

object PdfNativeGenerator {
  def main(args: Array[String]): Unit = {
    //    val gen = new PdfNativeGenerator("test2.pdf")
    //    gen.test()
    //    gen.close()
  }
}
