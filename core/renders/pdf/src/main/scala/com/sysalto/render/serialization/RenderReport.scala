package com.sysalto.render.serialization

import java.security.MessageDigest

import com.sysalto.render.PdfDraw._
import com.sysalto.render.util.PageTree
import com.sysalto.render.util.fonts.parsers.{FontParser, RFontParserFamily}
import com.sysalto.render.util.wrapper.WordWrap
import com.sysalto.report.ReportTypes.{BoundaryRect, WrapBox}
import com.sysalto.report.{RFontAttribute, ReportTypes, WrapAlign}
import com.sysalto.report.reportTypes._
import com.sysalto.report.util.{PersistenceFactory, PersistenceUtil}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

class RenderReport(name: String, PAGE_WIDTH: Float, PAGE_HEIGHT: Float, persistenceFactory: PersistenceFactory, pdfCompression: Boolean) {
	implicit val wordSeparators: List[Char] = List(',', '.')
	private[this] val fontFamilyMap = scala.collection.mutable.HashMap.empty[String, RFontParserFamily]
	private[this] val wordWrap = new WordWrap(fontFamilyMap)
	val renderReportTypes = new RenderReportTypes(persistenceFactory)
	private[this] val pdfWriter = new renderReportTypes.PdfWriter(name)
	private[this] var id: Long = 0
	private[this] var catalog: renderReportTypes.PdfCatalog = null
	private[this] var currentPage: renderReportTypes.PdfPage = null
	private[this] var fontMap = scala.collection.mutable.HashMap.empty[String, renderReportTypes.PdfFont]
	private[this] val txtList = ListBuffer[renderReportTypes.PdfTxtFragment]()
	private[this] val graphicList = ListBuffer[PdfGraphicFragment]()
	private[this] val pageList = ListBuffer[renderReportTypes.PdfPage]()
	private[this] var fontId: Long = 0


	private[this] implicit val allItems = mutable.HashMap[Long, renderReportTypes.PdfBaseItem]()


	def startPdf(): Unit = {
		pdfWriter <<< "%PDF-1.7"
		pdfWriter <<< s"%${128.toChar}${129.toChar}${130.toChar}${131.toChar}"
		catalog = new renderReportTypes.PdfCatalog(nextId())
		renderReportTypes.setObject(catalog)
		currentPage = new renderReportTypes.PdfPage(nextId(), 0, PAGE_WIDTH, PAGE_HEIGHT)
	}

	def newPage(): Unit = {
		saveCurrentPage()
		currentPage = new renderReportTypes.PdfPage(nextId(), 0, PAGE_WIDTH, PAGE_HEIGHT, fontMap.values.map(fontItem => fontItem.id).toList)
	}

	private[this] def saveCurrentPage(): Unit = {
		graphicList.foreach(item => {
			item.updateContent(this)
		})
		val text = new renderReportTypes.PdfText(txtList.toList)
		val graphic = new renderReportTypes.PdfGraphic(graphicList.toList)
		val pdfPageContent = new renderReportTypes.PdfPageContent(nextId(), List(graphic, text), pdfCompression)
		renderReportTypes.setObject(pdfPageContent)
		currentPage.idContentPageOpt = Some(pdfPageContent.id)
		currentPage.idFontList = fontMap.values.toList.sortBy(font => font.refName).map(font => font.id)
		renderReportTypes.setObject(currentPage)
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
				 				 |""".stripMargin.getBytes(renderReportTypes.ENCODING)
		val offset = pdfWriter.position
		pdfWriter << s
		(id, offset)
	}

	def md5(s: String) = {
		val result = MessageDigest.getInstance("MD5").digest(s.getBytes(renderReportTypes.ENCODING))
		javax.xml.bind.DatatypeConverter.printHexBinary(result)
	}


	def done(): Unit = {
		saveCurrentPage()

		val pageTreeList = PageTree.pageTree(pageList.toList) {
			() => {
				val pg = new renderReportTypes.PdfPageList(nextId())
				renderReportTypes.setObject(pg)
				pg
			}
		}.asInstanceOf[renderReportTypes.PdfPageList]
		catalog.idPdfPageListOpt = Some(pageTreeList.id)
		renderReportTypes.setObject(catalog)
		val allItems1 = renderReportTypes.getAllItems()
		allItems1.foreach(itemId => {
			val item = renderReportTypes.getObject[renderReportTypes.PdfBaseItem](itemId)
			item.write(pdfWriter)
		})
		val metaDataObj = metaData()
		val metaDataId = metaDataObj._1
		val xrefOffset = pdfWriter.position
		pdfWriter <<< "xref"

		pdfWriter <<< s"0 ${allItems1.length + 2}"
		pdfWriter <<< "0000000000 65535 f "
		allItems1.foreach(itemId => {
			val item = renderReportTypes.getObject[renderReportTypes.PdfBaseItem](itemId)
			val offset = item.offset.toString
			val offsetFrmt = "0" * (10 - offset.length) + offset
			pdfWriter <<< s"${offsetFrmt} 00000 n "
		})
		val metaOffset = metaDataObj._2.toString
		val offsetFrmt = "0" * (10 - metaOffset.length) + metaOffset
		pdfWriter <<< s"${offsetFrmt} 00000 n "
		pdfWriter <<< "trailer"
		pdfWriter <<< s"<</Size ${allItems1.length + 2}"
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
		fontFamilyMap += externalFont.name -> new RFontParserFamily(externalFont.name, externalFont, false)
	}

	private[this] def initEmbeddedFonts(): Unit = {
		initEmbeddedFont(RFontFamily("Courier", "Courier", Some("Courier-Bold"), Some("Courier-Oblique"), Some("Courier-BoldOblique")))
		initEmbeddedFont(RFontFamily("Helvetica", "Helvetica", Some("Helvetica-Bold"), Some("Helvetica-Oblique"), Some("Helvetica-BoldOblique")))
		initEmbeddedFont(RFontFamily("Times", "Times-Roman", Some("Times-Bold"), Some("Times-Italic"), Some("Times-BoldItalic")))
	}

	private[this] def initEmbeddedFont(rFontFamily: RFontFamily) {
		fontFamilyMap += rFontFamily.name -> new RFontParserFamily(rFontFamily.name, rFontFamily, true)
	}

	def close(): Unit = {
		pdfWriter.close()
		renderReportTypes.close
	}


	def line(x1: Float, y1: Float, x2: Float, y2: Float, lineWidth: Float, color: ReportColor, lineDashType: Option[LineDashType]): Unit = {
		graphicList += new DrawLine(x1, y1, x2, y2, lineWidth, color, lineDashType)
	}

	def rectangle(x1: Float, y1: Float, x2: Float, y2: Float,
	              radius: Float, color: Option[ReportColor] = None,
	              fillColor: Option[ReportColor] = None, idPaternColor: Option[Long] = None): Unit = {
		graphicList += new renderReportTypes.PdfRectangle(x1.toLong, y1.toLong, x2.toLong, y2.toLong, radius, color, fillColor, idPaternColor)
	}

	def directDrawMovePoint(x: Float, y: Float): Unit = {
		graphicList += new renderReportTypes.DirectDrawMovePoint(x, y)
	}

	def directDrawLine(x: Float, y: Float): Unit = {
		graphicList += new renderReportTypes.DirectDrawLine(x, y)
	}

	def directDraw(code:String): Unit = {
		graphicList += new renderReportTypes.DirectDraw(code)
	}

	def directDrawCircle(x: Float, y: Float,radius:Float): Unit = {
		graphicList += new renderReportTypes.DirectDrawCircle(x, y,radius)
	}

	def directDrawArc(x: Float, y: Float,radius:Float,startAngle:Float,endAngle:Float): Unit = {
		graphicList += new renderReportTypes.DirectDrawArc(x, y,radius,startAngle,endAngle)
	}

	def directDrawStroke(reportColor: ReportColor): Unit = {
		graphicList += new renderReportTypes.DirectDrawStroke(reportColor)
	}

	def directFillStroke(fill: Boolean, stroke: Boolean): Unit = {
		graphicList += new renderReportTypes.DirectFillStroke(fill, stroke)
	}

	def directDrawRectangle(x: Float, y: Float, width: Float, height: Float): Unit = {
		graphicList += new renderReportTypes.DirectDrawRectangle(x, y, width, height)
	}

	def arc(center: DrawPoint, radius: Float, startAngle: Float, endAngle: Float): Unit = {
		graphicList += new DrawArc(center, radius, startAngle, endAngle)
	}

	def circle(center: DrawPoint, radius: Float): Unit = {
		graphicList += new DrawCircle(center, radius)
	}

	def stroke() = {
		graphicList += new DrawStroke()
	}

	def wrap(txtList: List[ReportTxt], x0: Float, y0: Float, x1: Float, y1: Float,
	         wrapAlign: WrapAlign.Value, simulate: Boolean, lineHeight: Float): Option[ReportTypes.WrapBox] = {

		val lines = wordWrap.wordWrap(txtList, x1 - x0)
		var crtY = y0
		if (!simulate) {
			lines.foreach(line => {
				val l1: List[Float] = line.map(item => item.textLength)
				val length = l1.sum
				val newX = wrapAlign match {
					case WrapAlign.WRAP_CENTER => x0 + (x1 - x0 - length) * 0.5f
					case WrapAlign.WRAP_RIGHT => x1 - length
					case _ => x0
				}
				line.zipWithIndex.foreach {
					case (textPos, index) => {
						val offset = line.take(index).map(item => item.textLength).sum
						text(newX + offset, crtY, textPos.rtext)
					}
				}
				crtY -= lineHeight
			})
		} else {
			crtY -= lineHeight * (lines.size - 1)
		}
		val l1 = lines.head.map(textPos => wordWrap.getTextHeight(textPos.rtext))
		val textHeight = if (l1.isEmpty) 0 else l1.max
		Some(new WrapBox(PAGE_HEIGHT - y0, PAGE_HEIGHT - crtY, lines.size, textHeight))
	}


	private[this] def getFontParser(font: RFont): FontParser = {
		val fontFamily = fontFamilyMap(font.fontName)
		font.attribute match {
			case RFontAttribute.NORMAL => fontFamily.regular
			case RFontAttribute.BOLD => fontFamily.bold.get
			case RFontAttribute.ITALIC => fontFamily.italic.get
			case RFontAttribute.BOLD_ITALIC => fontFamily.boldItalic.get
		}
	}


	def getTextWidth(txt: ReportTxt): Float = wordWrap.getTextWidth(txt)

	def getTextWidth(cell: ReportCell): List[Float] = {
		val lines = wordWrap.wordWrap(cell.txt, cell.margin.right - cell.margin.left)
		lines.map(line => {
			val lastWord = line.last
			line.map(word => word.textLength - (if (word == lastWord) wordWrap.getTextWidth(ReportTxt(" ", word.rtext.font)) else 0)).sum
		})
	}

	def axialShade(x1: Float, y1: Float, x2: Float, y2: Float, rectangle: ReportTypes.DRectangle, from: ReportColor, to: ReportColor): Unit = {

		val colorFct = new renderReportTypes.PdfShaddingFctColor(nextId(), from, to)
		renderReportTypes.setObject(colorFct)
		val pdfShadding = new renderReportTypes.PdfColorShadding(nextId(), x1, y1, x1, y2, colorFct.id)
		renderReportTypes.setObject(pdfShadding)
		val pattern = new renderReportTypes.PdfGPattern(nextId(), pdfShadding.id)
		renderReportTypes.setObject(pattern)
		currentPage.idPdfPatternList ++= List(pattern.id)
		renderReportTypes.setObject(currentPage)
		this.rectangle(rectangle.x1, rectangle.y1, rectangle.x2, rectangle.y2, 0, None, None, Some(pattern.id))
		this.stroke()
	}


	def drawImage(file: String, x: Float, y: Float, width: Float, height: Float, opacity: Float): Unit = {
		val pdfImage = new renderReportTypes.PdfImage(nextId(), file)
		renderReportTypes.setObject(pdfImage)
		val scale = Math.min(width / pdfImage.imageMeta.width, height / pdfImage.imageMeta.height)
		graphicList += new renderReportTypes.PdfDrawImage(pdfImage.id, x, y, scale)
		currentPage.idImageList += pdfImage.id
	}

	def drawPieChart(font: RFont, title: String, data: List[(String, Double)], x: Float, y: Float, width: Float, height: Float): Unit = {
		graphicList += new renderReportTypes.DrawPieChart1(font, title, data, x, y, width, height)
	}


	def text(x: Float, y: Float, txt: ReportTxt): Unit = {
		val font = if (!fontMap.contains(txt.font.fontKeyName)) {
			if (txt.font.externalFont.isDefined) {
				val fontParser = getFontParser(txt.font)
				val fontStream = new renderReportTypes.PdfFontStream(nextId(), fontParser.fontName, fontParser.fontMetric, pdfCompression)
				renderReportTypes.setObject(fontStream)
				val fontDescr = new renderReportTypes.PdfFontDescriptor(nextId(), fontStream.id, txt.font.fontKeyName)
				renderReportTypes.setObject(fontDescr)
				val font1 = new renderReportTypes.PdfFont(nextId(), nextFontId(), txt.font.fontKeyName,
					Some(new renderReportTypes.FontEmbeddedDef(fontDescr.id, fontStream.id)))
				renderReportTypes.setObject(font1)
				fontMap += (txt.font.fontKeyName -> font1)
				font1
			} else {
				val font1 = new renderReportTypes.PdfFont(nextId(), nextFontId(), txt.font.fontKeyName)
				renderReportTypes.setObject(font1)
				fontMap += (txt.font.fontKeyName -> font1)
				font1
			}
		}
		else fontMap(txt.font.fontKeyName)
		txtList += new renderReportTypes.PdfTxtFragment(x, y, txt, font.refName)
	}


	def linkToPage(boundaryRect: BoundaryRect, pageNbr: Long, left: Int, top: Int): Unit = {
		val goto = new renderReportTypes.PdfGoToPage(nextId(), pageNbr, left, top)
		renderReportTypes.setObject(goto)
		val pdfLink = new renderReportTypes.PdfLink(nextId(), boundaryRect, goto.id)
		renderReportTypes.setObject(pdfLink)
		currentPage.idAnnotationList = currentPage.idAnnotationList ::: List(pdfLink.id)
	}

	def linkToUrl(boundaryRect: BoundaryRect, url: String): Unit = {
		val goto = new renderReportTypes.PdfGoToUrl(nextId(), url)
		renderReportTypes.setObject(goto)
		val pdfLink = new renderReportTypes.PdfLink(nextId(), boundaryRect, goto.id)
		renderReportTypes.setObject(pdfLink)
		currentPage.idAnnotationList = currentPage.idAnnotationList ::: List(pdfLink.id)
	}


	initEmbeddedFonts()

}
