package com.sysalto.render

import java.io.{File, PrintWriter}

import com.sysalto.report.RFontAttribute
import com.sysalto.report.reportTypes.{LineDashType, RColor, RText, ReportPageOrientation}
import pdfGenerator.PageTree

import scala.collection.mutable.ListBuffer

/**
  * Created by marian on 4/1/17.
  */
class PdfNativeGenerator(name: String, val orientation: ReportPageOrientation.Value) {
  implicit val pdfWriter = new PdfWriter(name)
  implicit val allItems = ListBuffer[PdfBaseItem]()
  val txtList = ListBuffer[PdfTxtChuck]()
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

  }

  def text(x: Float, y: Float, txt: RText): Unit = {
    val font = if (!fontMap.contains(txt.font.fontKeyName)) {
      val font1 = new PdfFont(nextId(), nextFontId(), txt.font.fontKeyName)
      fontMap += (txt.font.fontKeyName -> font1)
      font1
    } else fontMap.get(txt.font.fontKeyName).get
    txtList += PdfTxtChuck(x, y, txt, font.refName)
  }

  def startPdf(): Unit = {
    pdfHeader()
    catalog = new PdfCatalog(nextId())
    val outline = new PdfOutline(nextId())
    catalog.outline = Some(outline)
    currentPage = new PdfPage(nextId(), 0, orientation, None)
  }

  def newPage(): Unit = {
    saveCurrentPage
    currentPage = new PdfPage(nextId(), 0, orientation, None, fontMap.values.toList)
  }

  def saveCurrentPage(): Unit = {
    val text = new PdfText(currentPage, nextId(), txtList.toList)
    currentPage.contentPage = Some(text)
    currentPage.fontList = fontMap.values.toList.sortBy(font => font.refName)
    pageList += currentPage
    txtList.clear()
  }

  def done(): Unit = {
    saveCurrentPage

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

  def content: String

  def write(pdfWriter: PdfWriter): Unit = {
    offset = pdfWriter.position
    pdfWriter <<< content.trim
  }

  itemList += this

  override def toString: String = {
    s"[${this.getClass.getTypeName}]\n" + content
  }
}

class PdfCatalog(id: Long, var outline: Option[PdfOutline] = None, var pdfPageList: Option[PdfPageList] = None)
                (implicit itemList: ListBuffer[PdfBaseItem]) extends PdfBaseItem(id) {
  override def content: String = {
    s"""${id} 0 obj
       |  <<  /Type /Catalog
       |      /Outlines ${outline.get.id} 0 R
       |      /Pages ${pdfPageList.get.id} 0 R
       |  >>
       |endobj
     """.stripMargin
  }
}

class PdfOutline(id: Long)(implicit itemList: ListBuffer[PdfBaseItem]) extends PdfBaseItem(id) {
  override def content: String = {
    s"""${id} 0 obj
       |  <<  /Type /Outlines
       |      /Count 0
       |  >>
       |endobj
     """.stripMargin
  }
}

class PdfPageList(id: Long, parentId: Option[Long] = None, var pageList: List[Long] = List())
                 (implicit itemList: ListBuffer[PdfBaseItem]) extends PdfBaseItem(id) {
  override def content: String = {
    val parentIdStr = if (parentId.isDefined) s"/Parent ${parentId.get} 0 R" else ""
    val pageListStr = pageList.map(id => id + " 0 R").mkString("\n")
    s"""${id} 0 obj
       |  <<  /Type /Pages ${parentIdStr}
       |      /Kids [ ${pageListStr} ]
       |      /Count ${pageList.length}
       |  >>
       |endobj
     """.stripMargin
  }
}

class PdfPage(id: Long, var pdfPageListId: Long = 0, val orientation: ReportPageOrientation.Value = ReportPageOrientation.PORTRAIT,
              var procSet: Option[PdfProcSet] = None,
              var fontList: List[PdfFont] = List(), var contentPage: Option[PdfContent] = None)
             (implicit itemList: ListBuffer[PdfBaseItem]) extends PdfBaseItem(id) {
  override def content: String = {
    val contentStr = if (contentPage.isDefined) s"/Contents ${contentPage.get.id} 0 R" else ""
    val procSetStr = if (procSet.isDefined) s"/ProcSet ${procSet.get.id} 0 R" else ""
    val fontStr = "/Font<<" + fontList.map(font => s"/${font.refName} ${font.id} 0 R").mkString("") + ">>"
    s"""${id} 0 obj
       |  <<  /Type /Page
       |      /Parent ${pdfPageListId} 0 R
       |      /MediaBox [ 0 0 612 792 ] ${if (orientation == ReportPageOrientation.LANDCAPE) "/Rotate 90" else ""}
       |      ${contentStr}
       |      /Resources  <<   ${procSetStr}
       |                        ${fontStr}
       |                  >>
       |  >>
       |endobj
     """.stripMargin
  }
}


class PdfProcSet(id: Long)(implicit itemList: ListBuffer[PdfBaseItem]) extends PdfBaseItem(id) {
  override def content: String = {
    s"""${id} 0 obj
       |  [/PDF /Text ]
       |endobj
     """.stripMargin
  }
}

class PdfFont(id: Long, val refName: String, fontKeyName: String)(implicit itemList: ListBuffer[PdfBaseItem]) extends PdfBaseItem(id) {
  override def content: String = {
    s"""${id} 0 obj
       |  <<  /Type /Font
       |      /Subtype /Type1
       |      /BaseFont /${fontKeyName}
       |      /Encoding /WinAnsiEncoding
       |  >>
       |endobj
     """.stripMargin
  }
}

abstract class PdfContent(id: Long)(implicit itemList: ListBuffer[PdfBaseItem]) extends PdfBaseItem(id)
abstract class PdfGraphicContent(id: Long)(implicit itemList: ListBuffer[PdfBaseItem]) extends PdfBaseItem(id)


case class PdfTxtChuck(x: Float, y: Float, rtext: RText, fontRefName: String)

class PdfText(pdfPage: PdfPage, id: Long, txtList: List[PdfTxtChuck])
             (implicit itemList: ListBuffer[PdfBaseItem]) extends PdfContent(id) {
  override def content: String = { // landscape
    val portraitMatrix = "1 0 0 1 0 792 cm -1 0 0 -1 0 0 cm"
    val landscapeMatrix = "0 -1 1 0 0 0 cm"
    val s1 =
      s"""${if (pdfPage.orientation == ReportPageOrientation.PORTRAIT) portraitMatrix else landscapeMatrix}
         |BT""".stripMargin

    val s2 = txtList.map(item => {
      s"""  /${item.fontRefName} ${item.rtext.font.size} Tf
         |  -1 0 0 -1 -${item.x.toLong} ${item.y.toLong} Tm
         |        ( ${item.rtext.txt} ) Tj
       """.stripMargin
    }).foldLeft("")((s1, s2) => s1 + s2)

    val s3 =
      s"""${s1}
         |${s2}
         |      ET
       """.stripMargin

    s"""${id} 0 obj
       |  <<  /Length ${s3.length} >>
       |      stream
       |${s3}endstream
       |endobj
     """.stripMargin
  }

}



class PdfTextOld(pdfPage: PdfPage, id: Long, txtList: List[PdfTxtChuck])
             (implicit itemList: ListBuffer[PdfBaseItem]) extends PdfContent(id) {
  override def content: String = { // landscape
    val portraitMatrix = "1 0 0 1 0 792 cm -1 0 0 -1 0 0 cm"
    val landscapeMatrix = "0 -1 1 0 0 0 cm"
    val s1 =
      s"""${if (pdfPage.orientation == ReportPageOrientation.PORTRAIT) portraitMatrix else landscapeMatrix}
         |BT
         |        /F1 10 Tf""".stripMargin

    val s2 = txtList.map(item => {
      s"""        -1 0 0 -1 -${item.x.toLong} ${item.y.toLong} Tm
         |        ( ${item.rtext.txt} ) Tj
       """.stripMargin
    }).foldLeft("")((s1, s2) => s1 + s2)

    val s3 =
      s"""${s1}
         |${s2}
         |      ET
       """.stripMargin

    s"""${id} 0 obj
       |  <<  /Length ${s3.length} >>
       |      stream
       |${s3}endstream
       |endobj
     """.stripMargin
  }

}


class PdfWriter(name: String) {
  new File(name).delete()
  val writer = new PrintWriter(name)
  var position: Long = 0

  def <<(str: String) = {
    writer.print(str)
    position += str.length
  }

  def <<<(str: String) = {
    val str1 = str + "\n"
    writer.print(str1)
    position += str1.length
  }

  def close(): Unit = {
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
