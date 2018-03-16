package com.sysalto.render.serialization

import java.security.MessageDigest

import com.sysalto.render.PdfDraw.PdfGraphicFragment
import com.sysalto.render.serialization.RenderReportTypes._
import com.sysalto.render.util.PageTree
import com.sysalto.render.util.fonts.parsers.RFontParserFamily
import com.sysalto.render.util.wrapper.WordWrap
import com.sysalto.report.reportTypes.RFontFamily
import com.sysalto.report.util.RockDbUtil

import scala.collection.mutable.ListBuffer

class RenderReport(name: String, PAGE_WIDTH: Float, PAGE_HEIGHT: Float, pdfCompression: Boolean) {
	implicit val wordSeparators = List(',', '.')
	private[serialization] val db = RockDbUtil()
	private[this] val fontFamilyMap = scala.collection.mutable.HashMap.empty[String, RFontParserFamily]
	private[this] val wordWrap = new WordWrap(fontFamilyMap)
	private[this] val pdfWriter = new PdfWriter(name)
	private[this] var id: Long = 0
	private[this] var catalog: PdfCatalog = null
	private[this] var currentPage: PdfPage = null
	private[this] var fontMap = scala.collection.mutable.HashMap.empty[String, PdfFont]
	private[this] val txtList = ListBuffer[PdfTxtFragment]()
	private[this] val graphicList = ListBuffer[PdfGraphicFragment]()
	private[this] val pageList = ListBuffer[PdfPage]()
	private[this] var fontId: Long = 0


	def startPdf(): Unit = {
		pdfWriter <<< "%PDF-1.7"
		pdfWriter <<< s"%${128.toChar}${129.toChar}${130.toChar}${131.toChar}"
		catalog = new PdfCatalog(nextId())
		currentPage = new PdfPage(nextId(), 0, PAGE_WIDTH, PAGE_HEIGHT)
	}

	def newPage(): Unit = {
		saveCurrentPage()
		currentPage = new PdfPage(nextId(), 0, PAGE_WIDTH, PAGE_HEIGHT, fontMap.values.map(fontItem => fontItem.id).toList)
	}

	private[this] def saveCurrentPage(): Unit = {
		val text = new PdfText(txtList.toList)
		RenderReportTypes.setObject(text)
		val graphic = new PdfGraphic(graphicList.toList)
		RenderReportTypes.setObject(graphic)
		val pdfPageContext = new PdfPageContent(nextId(), List(graphic, text), pdfCompression)
		RenderReportTypes.setObject(pdfPageContext)
		currentPage.idContentPageOpt = Some(pdfPageContext.id)
		currentPage.idFontList = fontMap.values.toList.sortBy(font => font.refName).map(font => font.id)
		pageList += currentPage
		txtList.clear()
		graphicList.clear()
	}

	def metaData(): (Long, Long) = {
		val id = nextId()
		val s =
			s"""${id} 0 obj
				 				 |  <<  /Producer (Reactive Reports - Copyright 2018 SysAlto Corporation)
				 				 |  >>
				 				 |endobj
				 				 |""".stripMargin.getBytes(RenderReportTypes.ENCODING)
		val offset = pdfWriter.position
		pdfWriter << s
		(id, offset)
	}

	def md5(s: String) = {
		val result = MessageDigest.getInstance("MD5").digest(s.getBytes(RenderReportTypes.ENCODING))
		javax.xml.bind.DatatypeConverter.printHexBinary(result)
	}


	def done(): Unit = {
		saveCurrentPage()

		val pageTreeList = PageTree.pageTree(pageList.toList) {
			() => {
				val pg = new PdfPageList(nextId())
				RenderReportTypes.setObject(pg)
				pg
			}
		}.asInstanceOf[PdfPageList]
		RenderReportTypes.setObject(pageTreeList)
		catalog.idPdfPageListOpt = Some(pageTreeList.id)
		val allItems = RenderReportTypes.getAllItems()
		allItems.foreach(itemId => {
			val item = RenderReportTypes.getObject[PdfBaseItem](itemId)
			item.write(pdfWriter)
		})
		val metaDataObj = metaData()
		val metaDataId = metaDataObj._1
		val xrefOffset = pdfWriter.position
		pdfWriter <<< "xref"

		pdfWriter <<< s"0 ${allItems.length + 2}"
		pdfWriter <<< "0000000000 65535 f "
		allItems.foreach(itemId => {
			val item = RenderReportTypes.getObject[PdfBaseItem](itemId)
			val offset = item.offset.toString
			val offsetFrmt = "0" * (10 - offset.length) + offset
			pdfWriter <<< s"${offsetFrmt} 00000 n "
		})
		val metaOffset = metaDataObj._2.toString
		val offsetFrmt = "0" * (10 - metaOffset.length) + metaOffset
		pdfWriter <<< s"${offsetFrmt} 00000 n "
		pdfWriter <<< "trailer"
		pdfWriter <<< s"<</Size ${allItems.length + 2}"
		pdfWriter <<< "   /Root 1 0 R"
		pdfWriter <<< s"   /Info ${metaDataId} 0 R"
		val fileId = md5(name + System.currentTimeMillis())
		pdfWriter <<< s"   /ID [<${fileId}><${fileId}>]"
		pdfWriter <<< ">>"
		pdfWriter <<< "startxref"
		pdfWriter <<< xrefOffset.toString
		pdfWriter <<< "%%EOF"
	}

	private[serialization] def nextId(): Long = {
		id += 1
		id
	}

	def nextFontId(): String = {
		fontId += 1
		"F" + fontId
	}

	def setExternalFont(externalFont: RFontFamily): Unit = {
		fontFamilyMap += externalFont.name -> RFontParserFamily(externalFont.name, externalFont, false)
	}

	private[this] def initEmbeddedFonts(): Unit = {
		initEmbeddedFont(RFontFamily("Courier", "Courier", Some("Courier-Bold"), Some("Courier-Oblique"), Some("Courier-BoldOblique")))
		initEmbeddedFont(RFontFamily("Helvetica", "Helvetica", Some("Helvetica-Bold"), Some("Helvetica-Oblique"), Some("Helvetica-BoldOblique")))
		initEmbeddedFont(RFontFamily("Times", "Times-Roman", Some("Times-Bold"), Some("Times-Italic"), Some("Times-BoldItalic")))
	}

	private[this] def initEmbeddedFont(rFontFamily: RFontFamily) {
		fontFamilyMap += rFontFamily.name -> RFontParserFamily(rFontFamily.name, rFontFamily, true)
	}

	def close(): Unit = {
		pdfWriter.close()
		db.close()
	}


}
