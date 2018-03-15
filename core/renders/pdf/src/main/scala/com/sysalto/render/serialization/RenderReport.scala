package com.sysalto.render.serialization

import com.sysalto.render.PdfDraw.PdfGraphicFragment
import com.sysalto.render.serialization.RenderReportTypes._
import com.sysalto.render.util.fonts.parsers.RFontParserFamily
import com.sysalto.render.util.wrapper.WordWrap
import com.sysalto.report.util.RockDbUtil

import scala.collection.mutable.ListBuffer

class RenderReport(name: String, PAGE_WIDTH: Float, PAGE_HEIGHT: Float, pdfCompression: Boolean) {
	implicit val wordSeparators = List(',', '.')
	private[this] val db = RockDbUtil()
	private[this] val fontFamilyMap = scala.collection.mutable.HashMap.empty[String, RFontParserFamily]
	private[this] val wordWrap = new WordWrap(fontFamilyMap)
	private[this] val pdfWriter = new PdfWriter(name)
	private[this] var id: Long = 0
	private[this] var catalog: PdfCatalog = null
	private[this] var currentPage: PdfPage = null
	private[this] var fontMap = scala.collection.mutable.HashMap.empty[String, PdfFont]
	private[this] val txtList = ListBuffer[PdfTxtFragment]()
	private[this] val graphicList = ListBuffer[PdfGraphicFragment]()

	private[serialization] def nextId(): Long = {
		id += 1
		id
	}

	def startPdf(): Unit = {
		pdfWriter <<< "%PDF-1.7"
		pdfWriter <<< s"%${128.toChar}${129.toChar}${130.toChar}${131.toChar}"
		catalog = new PdfCatalog(nextId())
		currentPage = new PdfPage(nextId(), 0, PAGE_WIDTH, PAGE_HEIGHT)
	}

	def newPage(): Unit = {
		saveCurrentPage()
		currentPage = new PdfPage(nextId(), 0, PAGE_WIDTH, PAGE_HEIGHT, fontMap.values.map(fontItem=>fontItem.id).toList)
	}

	private[this] def saveCurrentPage(): Unit = {
		val text = new PdfText(txtList.toList)
		RenderReportTypes.setObject(text)
		val graphic = new PdfGraphic(graphicList.toList)
		RenderReportTypes.setObject(graphic)
		val pdfPageContext=new PdfPageContent(nextId(), List(graphic, text), pdfCompression)
		RenderReportTypes.setObject(pdfPageContext)
		currentPage.idContentPageOpt = Some(pdfPageContext.id)
		currentPage.idFontList = fontMap.values.toList.sortBy(font => font.refName).map(font=>font.id)
		txtList.clear()
		graphicList.clear()
	}


	def close(): Unit = {
		pdfWriter.close()
		db.close()
	}
}
