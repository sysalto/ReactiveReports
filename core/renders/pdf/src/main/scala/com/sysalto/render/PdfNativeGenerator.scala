/*
 * ReactiveReports - Free Java /Scala Reporting Library.
 * Copyright (C) 2017 SysAlto Corporation. All rights reserved.
 *
 * This program is part of ReactiveReports.
 *
 * ReactiveReports is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ReactiveReports is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY. Without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with ReactiveReports.
 * If not, see https://www.gnu.org/licenses/lgpl-3.0.en.html.
 */


package com.sysalto.render

import java.awt.image.BufferedImage
import java.io.{ByteArrayOutputStream, File, FileOutputStream, PrintWriter}
import java.nio.file.{Files, Paths, StandardOpenOption}
import java.security.MessageDigest
import java.util.zip.Deflater
import javax.imageio.ImageIO

import com.sysalto.render.PdfDraw._
import com.sysalto.report.ReportTypes.{BoundaryRect, WrapBox}
import com.sysalto.report.{RFontAttribute, ReportTypes, WrapAlign}
import com.sysalto.report.reportTypes._
import com.sysalto.report.util.RockDbUtil
import util.PageTree.PageNode
import util.fonts.parsers.FontParser.FontMetric
import util.{PageTree, SyncFileUtil}
import util.fonts.parsers.{AfmParser, FontParser, RFontParserFamily, TtfParser}
import util.wrapper.WordWrap

import scala.collection.mutable.ListBuffer

/**
	* Created by marian on 4/1/17.
	*/
class PdfNativeGenerator(name: String, PAGE_WIDTH: Float, PAGE_HEIGHT: Float, pdfCompression: Boolean) {
	private[this] val db = RockDbUtil()

	private[this] implicit val pdfWriter = new PdfWriter(name)
	private[this] implicit val allItems = ListBuffer[PdfBaseItem]()
	private[this] val txtList = ListBuffer[PdfTxtChuck]()
	private[this] val graphicList = ListBuffer[PdfGraphicChuck]()
	private[this] val fontFamilyMap = scala.collection.mutable.HashMap.empty[String, RFontParserFamily]
	private[this] val wordWrap = new WordWrap(fontFamilyMap)
	private[this] var id: Long = 0
	private[this] var fontId: Long = 0

	private[this] var catalog: PdfCatalog = null
	private[this] var fontMap = scala.collection.mutable.HashMap.empty[String, PdfFont]
	private[this] var currentPage: PdfPage = null
	private[this] val pageList = ListBuffer[PdfPage]()

	private[this] def pdfHeader(): Unit = {
		pdfWriter <<< "%PDF-1.7"
		pdfWriter <<< s"%${128.toChar}${129.toChar}${130.toChar}${131.toChar}"
	}


	def close(): Unit = {
		pdfWriter.close()
		db.close()
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

	def line(x1: Float, y1: Float, x2: Float, y2: Float, lineWidth: Float, color: RColor, lineDashType: Option[LineDashType]): Unit = {
		graphicList += DrawLine(x1, y1, x2, y2, lineWidth, color, lineDashType)
	}

	def rectangle(x1: Float, y1: Float, x2: Float, y2: Float,
	              radius: Float, color: Option[RColor] = None,
	              fillColor: Option[RColor] = None, paternColor: Option[PdfGPattern] = None): Unit = {
		graphicList += PdfRectangle(x1.toLong, y1.toLong, x2.toLong, y2.toLong, radius, color, fillColor, paternColor)
	}

	def arc(center: DrawPoint, radius: Float, startAngle: Float, endAngle: Float): Unit = {
		graphicList += DrawArc(center, radius, startAngle, endAngle)
	}

	def circle(center: DrawPoint, radius: Float): Unit = {
		graphicList += DrawCircle(center, radius)
	}

	def stroke() = {
		graphicList += DrawStroke()
	}

	def wrap(txtList: List[RText], x0: Float, y0: Float, x1: Float, y1: Float,
	         wrapAlign: WrapAlign.Value, simulate: Boolean, startY: Option[Float], lineHeight: Float): Option[ReportTypes.WrapBox] = {
		implicit val wordSeparators = List(',', '.')
		val lines = wordWrap.wordWrap(txtList, x1 - x0)
		var crtY = y0
		if (!simulate) {
			lines.foreach(line => {
				val l1: List[Float] = line.map(item => item.textLength)
				val length = l1.sum
				val newX = wrapAlign match {
					case WrapAlign.WRAP_CENTER => (x1 - x0-length)*0.5f
					case WrapAlign.WRAP_RIGHT => x1 - length
					case _ =>x0
				}
				line.foreach(textPos =>
					text(newX + textPos.x, crtY, textPos.rtext)
				)
				crtY -= lineHeight
			})
		} else {
			crtY -= lineHeight * (lines.size - 1)
		}
		Some(WrapBox(PAGE_HEIGHT - y0, PAGE_HEIGHT - crtY, lines.size))
	}

	def axialShade(x1: Float, y1: Float, x2: Float, y2: Float, rectangle: ReportTypes.DRectangle, from: RColor, to: RColor): Unit = {

		val colorFct = new PdfShaddingFctColor(nextId(), from, to)
		val pdfShadding = new PdfColorShadding(nextId(), x1, y1, x1, y2, colorFct)
		val pattern = new PdfGPattern(nextId(), pdfShadding)
		currentPage.pdfPatternList ++= List(pattern)
		this.rectangle(rectangle.x1, rectangle.y1, rectangle.x2, rectangle.y2, 0, None, None, Some(pattern))
		this.stroke()
	}


	def drawImage(file: String, x: Float, y: Float, width: Float, height: Float, opacity: Float): Unit = {
		val pdfImage = new PdfImage(nextId(), file)
		val scale = Math.min(width / pdfImage.imageMeta.width, height / pdfImage.imageMeta.height)
		graphicList += PdfDrawImage(pdfImage, x, y, scale)
		currentPage.imageList = List(pdfImage)
	}

	def drawPieChart(font: RFont, title: String, data: List[(String, Double)], x: Float, y: Float, width: Float, height: Float): Unit = {
		graphicList += DrawPieChart(this, font, title, data, x, y, width, height)
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

	def text(x: Float, y: Float, txt: RText): Unit = {
		val font = if (!fontMap.contains(txt.font.fontKeyName)) {
			if (txt.font.externalFont.isDefined) {
				val fontParser = getFontParser(txt.font)
				val fontStream = new PdfFontStream(nextId(), fontParser.fontName, fontParser.fontMetric, pdfCompression)
				val fontDescr = new PdfFontDescriptor(nextId(), fontStream, txt.font.fontKeyName)
				val font1 = new PdfFont(nextId(), nextFontId(), txt.font.fontKeyName,
					Some(FontEmbeddedDef(fontDescr, fontStream)))
				fontMap += (txt.font.fontKeyName -> font1)
				font1
			} else {
				val font1 = new PdfFont(nextId(), nextFontId(), txt.font.fontKeyName)
				fontMap += (txt.font.fontKeyName -> font1)
				font1
			}
		}
		else fontMap(txt.font.fontKeyName)
		txtList += PdfTxtChuck(x, y, txt, font.refName)
	}


	def linkToPage(boundaryRect: BoundaryRect, pageNbr: Long, left: Int, top: Int): Unit = {
		val goto = new PdfGoToPage(nextId(), pageNbr, left, top)
		val pdfLink = new PdfLink(nextId(), boundaryRect, goto)
		currentPage.annotation = currentPage.annotation ::: List(pdfLink)
	}

	def linkToUrl(boundaryRect: BoundaryRect, url: String): Unit = {
		val goto = new PdfGoToUrl(nextId(), url)
		val pdfLink = new PdfLink(nextId(), boundaryRect, goto)
		currentPage.annotation = currentPage.annotation ::: List(pdfLink)
	}

	def startPdf(): Unit = {
		pdfHeader()
		catalog = new PdfCatalog(nextId())
		currentPage = new PdfPage(nextId(), 0, PAGE_WIDTH, PAGE_HEIGHT)
	}

	def newPage(): Unit = {
		saveCurrentPage()
		currentPage = new PdfPage(nextId(), 0, PAGE_WIDTH, PAGE_HEIGHT, fontMap.values.toList)
	}

	def saveCurrentPage(): Unit = {
		val text = new PdfText(txtList.toList)
		val graphic = new PdfGraphic(graphicList.toList)
		currentPage.contentPage = Some(new PdfPageContent(nextId(), List(graphic, text), pdfCompression))
		currentPage.fontList = fontMap.values.toList.sortBy(font => font.refName)
		pageList += currentPage
		txtList.clear()
		graphicList.clear()
	}

	def metaData(): (Long, Long) = {
		val id = nextId()
		val s =
			s"""${id} 0 obj
				 				 |  <<  /Producer (Reactive Reports - Copyright 2017 SysAlto Corporation)
				 				 |  >>
				 				 |endobj
				 				 |""".stripMargin.getBytes
		val offset = pdfWriter.position
		pdfWriter << s
		(id, offset)
	}

	def md5(s: String) = {
		val result = MessageDigest.getInstance("MD5").digest(s.getBytes)
		javax.xml.bind.DatatypeConverter.printHexBinary(result)
	}

	def done(): Unit = {
		saveCurrentPage()

		val pageTreeList = PageTree.pageTree(pageList.toList) {
			() => {
				new PdfPageList(nextId())
			}
		}.asInstanceOf[PdfPageList]

		catalog.pdfPageList = Some(pageTreeList)
		allItems.foreach(item => item.write(pdfWriter))
		val metaDataObj = metaData()
		val metaDataId = metaDataObj._1
		val xrefOffset = pdfWriter.position
		pdfWriter <<< "xref"
		pdfWriter <<< s"0 ${allItems.length + 2}"
		pdfWriter <<< "0000000000 65535 f "
		allItems.foreach(item => {
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


	def nextId(): Long = {
		id += 1
		id
	}

	def nextFontId(): String = {
		fontId += 1
		"F" + fontId
	}

	initEmbeddedFonts()

}


object PdfNativeGenerator {
	def convertColor(color: RColor): (Float, Float, Float) = {
		val r = color.r / 255f
		val g = color.g / 255f
		val b = color.b / 255f
		(r, g, b)
	}

	def writeData(id: Long, input: Array[Byte], pdfCompression: Boolean, hasLength1: Boolean = false): Array[Byte] = {
		val length1 = if (hasLength1) s"/Length1 ${input.size}" else ""
		val result = if (!pdfCompression) {
			s"""${id} 0 obj
				 |<</Length ${input.length} ${length1}>>
				 |stream
				 |""".stripMargin.getBytes ++
				input ++
				s"""
					 |endstream
					 |endobj
					 |""".stripMargin.getBytes
		} else {
			val compresser = new Deflater(Deflater.BEST_COMPRESSION)
			compresser.setInput(input)
			compresser.finish()
			val output = new Array[Byte](input.length)
			val compressedDataLength = compresser.deflate(output)
			compresser.end()
			val compressTxt = output.take(compressedDataLength)
			s"""${id} 0 obj
				 |<</Filter/FlateDecode/Length ${compressTxt.length} ${length1}>>
				 |stream
				 |""".stripMargin.getBytes ++
				compressTxt ++
				s"""
					 |endstream
					 |endobj
					 |""".stripMargin.getBytes
		}
		result
	}
}

abstract class PdfBaseItem(val id: Long)(implicit itemList: ListBuffer[PdfBaseItem]) {
	var offset: Long = 0
	itemList += this

	def content: Array[Byte]

	def write(pdfWriter: PdfWriter): Unit = {
		offset = pdfWriter.position
		pdfWriter << content
	}

	override def toString: String = {
		s"[${this.getClass.getTypeName}]\n" + content
	}
}


private[this] class PdfNames(id: Long, val dests: PdfDests)
                            (implicit itemList: ListBuffer[PdfBaseItem]) extends PdfBaseItem(id) {
	override def content: Array[Byte] = {
		s"""${id} 0 obj
			 |<</Dests ${dests.id} 0 R>>
			 |endobj
			 |""".stripMargin.getBytes
	}
}

private[this] class PdfDests(id: Long, val dests: ListBuffer[(String, String)] = ListBuffer())
                            (implicit itemList: ListBuffer[PdfBaseItem]) extends PdfBaseItem(id) {
	override def content: Array[Byte] = {
		val head = dests.head
		s"""${id} 0 obj
			 |<</Names[(${head._1}) 2 0 R]>>
			 |endobj
			 |""".stripMargin.getBytes
	}
}

private[this] class PdfCatalog(id: Long, var pdfPageList: Option[PdfPageList] = None, var pdfNames: Option[PdfNames] = None)
                              (implicit itemList: ListBuffer[PdfBaseItem]) extends PdfBaseItem(id) {
	override def content: Array[Byte] = {
		val namesStr = if (pdfNames.isEmpty) "" else s"/Names ${pdfNames.get.id} 0 R"
		s"""${id} 0 obj
			 |<<  /Type /Catalog
			 |    /Pages ${pdfPageList.get.id} 0 R
			 |    ${namesStr}
			 |  >>
			 |endobj
			 |""".stripMargin.getBytes
	}
}

private[this] class PdfPageList(id: Long, var parentId: Option[Long] = None, var pageList: ListBuffer[Long] = ListBuffer())
                               (implicit itemList: ListBuffer[PdfBaseItem]) extends PdfBaseItem(id) with PageNode {

	override def addChild(child: PageNode): Unit = {
		child match {
			case pdfPageList: PdfPageList => {
				pdfPageList.parentId = Some(this.id)
				pageList += pdfPageList.id
				leafNbr += child.leafNbr
			}
			case pdfPage: PdfPage => {
				pageList += pdfPage.id
				pdfPage.parentId = id
				leafNbr += 1
			}
		}
	}

	override def content: Array[Byte] = {
		val parentIdStr = if (parentId.isDefined) s"/Parent ${parentId.get} 0 R" else ""
		val pageListStr = pageList.map(id => id + " 0 R").mkString("\n")
		s"""${id} 0 obj
			 			 |  <<  /Type /Pages ${parentIdStr}
			 			 |      /Kids [ ${pageListStr} ]
			 			 |      /Count ${leafNbr}
			 			 |  >>
			 			 |endobj
			 			 |""".stripMargin.getBytes
	}
}

private[this] class PdfShaddingFctColor(id: Long, color1: RColor, color2: RColor)
                                       (implicit itemList: ListBuffer[PdfBaseItem]) extends PdfBaseItem(id) {
	override def content: Array[Byte] = {
		val colorNbr1 = PdfNativeGenerator.convertColor(color1)
		val colorNbr2 = PdfNativeGenerator.convertColor(color2)

		s"""${id} 0 obj
			 			 |  <</FunctionType 2/Domain[0 1]/C0[${colorNbr1._1} ${colorNbr1._2} ${colorNbr1._3}]/C1[${colorNbr2._1} ${colorNbr2._2} ${colorNbr2._3}]/N 1>>
			 			 |endobj
			 			 |""".stripMargin.getBytes
	}
}

private[this] class PdfColorShadding(id: Long, x0: Float, y0: Float, x1: Float, y1: Float, pdfShaddingFctColor: PdfShaddingFctColor)
                                    (implicit itemList: ListBuffer[PdfBaseItem]) extends PdfBaseItem(id) {
	override def content: Array[Byte] = {
		s"""${id} 0 obj
			 			 |  <</ShadingType 2/ColorSpace/DeviceRGB/Coords[$x0 $y0  $x1 $y1]/Function ${pdfShaddingFctColor.id} 0 R>>
			 			 |endobj
			 			 |""".stripMargin.getBytes
	}
}

class PdfGPattern(id: Long, pdfShadding: PdfColorShadding)
                 (implicit itemList: ListBuffer[PdfBaseItem]) extends PdfBaseItem(id) {
	val name = "P" + id

	override def content: Array[Byte] = {
		s"""${id} 0 obj
			 			 |  <</PatternType 2/Shading ${pdfShadding.id} 0 R/Matrix[1 0 0 1 0 0]>>
			 			 |endobj
			 			 |""".stripMargin.getBytes
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

class PdfImage(id: Long, fileName: String)(implicit itemList: ListBuffer[PdfBaseItem]) extends PdfBaseItem(id) {
	val name = "img" + id
	val imageMeta = new ImageMeta(fileName)

	override def content: Array[Byte] = {
		s"""${id} 0 obj
			 			 |  <<
			 			 | /Type /XObject
			 			 | /Subtype /Image
			 			 | /Width ${imageMeta.width}
			 			 | /Height ${imageMeta.height}
			 			 |  /ColorSpace /DeviceRGB
			 			 |  /BitsPerComponent ${imageMeta.pixelSize}
			 			 |  /Length ${imageMeta.imageInByte.length}
			 			 |  /Filter /DCTDecode
			 			 |  >>
			 			 |stream
			 			 |""".stripMargin.getBytes ++
			imageMeta.imageInByte ++
			"\nendstream\nendobj\n".getBytes
	}
}


private case class PdfDrawImage(pdfImage: PdfImage, x: Float, y: Float, scale: Float = 1, opacity: Option[Float] = None)
                               (implicit itemList: ListBuffer[PdfBaseItem]) extends PdfGraphicChuck {
	private[this] val image = pdfImage.imageMeta
	private[this] val width = image.width * scale
	private[this] val height = image.height * scale
	private[this] val opacityStr = ""

	def content: String =
		s"""q
			 			 |$opacityStr
			 			 |$width 0 0 $height ${x} ${y} cm
			 			 |/${pdfImage.name} Do
			 			 | Q
    """.stripMargin

}

class PdfPage(id: Long, var parentId: Long = 0, var pageWidth: Float, var pageHeight: Float,
              var fontList: List[PdfFont] = List(), var pdfPatternList: List[PdfGPattern] = List(),
              var annotation: List[PdfAnnotation] = List(),
              var imageList: List[PdfImage] = List(), var contentPage: Option[PdfPageContent] = None)
             (implicit itemList: ListBuffer[PdfBaseItem]) extends PdfBaseItem(id) with PageNode {

	override def addChild(child: PageNode): Unit = {}

	override def content: Array[Byte] = {
		val contentStr = if (contentPage.isDefined) s"/Contents ${contentPage.get.id} 0 R" else ""
		val fontStr = "/Font<<" + fontList.map(font => s"/${font.refName} ${font.id} 0 R").mkString("") + ">>"
		val patternStr = if (pdfPatternList.isEmpty) "" else "/Pattern <<" + pdfPatternList.map(item => s"/${item.name} ${item.id} 0 R").mkString(" ") + ">>"
		val imageStr = if (imageList.isEmpty) "" else "/XObject <<" + imageList.map(item => s"/${item.name} ${item.id} 0 R").mkString(" ") + ">>"
		val annotsStr = if (annotation.isEmpty) "" else "/Annots [" + annotation.map(item => s"${item.id} 0 R").mkString(" ") + "]"
		val result =
			s"""${id} 0 obj
				 |<<  /Type /Page
				 |      /Parent ${parentId} 0 R
				 |      /MediaBox [ 0 0 ${pageWidth} ${pageHeight} ]
				 |      /TrimBox [ 0 0 ${pageWidth} ${pageHeight} ]
				 |      ${contentStr}
				 |      ${annotsStr}
				 |      /Resources
				 |        <<  ${fontStr}
				 |            ${patternStr}
				 |            ${imageStr}
				 |        >>
				 |>>
				 |endobj
				 |""".stripMargin
		result.replaceAll("(?m)^\\s+\\n", "").getBytes
	}
}

case class FontEmbeddedDef(pdfFontDescriptor: PdfFontDescriptor, pdfFontStream: PdfFontStream)


abstract class PdfAction(id: Long)(implicit itemList: ListBuffer[PdfBaseItem]) extends PdfBaseItem(id)

class PdfGoToUrl(id: Long, url: String)
                (implicit itemList: ListBuffer[PdfBaseItem]) extends PdfAction(id) {
	override def content: Array[Byte] = {
		s"""${id} 0 obj
			 |<<
			 |  /Type /Action
			 |  /S /URI
			 |  /IsMap false
			 |  /URI(${url})
			 |>>
			 |endobj
			 |""".stripMargin.getBytes
	}
}

class PdfGoToPage(id: Long, pageNbr: Long, left: Int, top: Int)
                 (implicit itemList: ListBuffer[PdfBaseItem]) extends PdfAction(id) {
	override def content: Array[Byte] = {
		s"""${id} 0 obj
			 |<<
			 |  /Type /Action
			 |  /S /GoTo
			 |  /D [ ${pageNbr - 1} /Fit ]
			 |>>
			 |endobj
			 |""".stripMargin.getBytes
	}
}

abstract class PdfAnnotation(id: Long)(implicit itemList: ListBuffer[PdfBaseItem]) extends PdfBaseItem(id)

private[this] class PdfLink(id: Long, boundaryRect: BoundaryRect, action: PdfAction)(implicit itemList: ListBuffer[PdfBaseItem]) extends PdfAnnotation(id) {
	override def content: Array[Byte] = {
		s"""${id} 0 obj
			 |  << /Type /Annot
			 |  /Subtype /Link
			 |  /Rect [${boundaryRect}]
			 |  /F 4
			 |  /Border [ 0 0 0 ]
			 |  /A ${action.id} 0 R
			 |>>
			 |endobj
			 |""".stripMargin.getBytes
	}
}

class PdfFont(id: Long, val refName: String, fontKeyName: String,
              embeddedDefOpt: Option[FontEmbeddedDef] = None)
             (implicit itemList: ListBuffer[PdfBaseItem]) extends PdfBaseItem(id) {
	override def content: Array[Byte] = {
		if (embeddedDefOpt.isEmpty) {
			s"""${id} 0 obj
				 |<<  /Type /Font
				 |/Subtype /Type1
				 |/BaseFont /${fontKeyName}
				 |/Encoding /WinAnsiEncoding
				 |>>
				 |endobj
				 |""".stripMargin.getBytes
		} else {
			val fontEmbedeedDef = embeddedDefOpt.get
			val withObj = fontEmbedeedDef.pdfFontStream.fontMetric.fontDescriptor.get.glyphWidth
			val firstChar = withObj.firstChar
			val lastChar = withObj.lastChar
			s"""${id} 0 obj
				 | << /Type/Font
				 |   /Subtype/TrueType
				 |   /BaseFont/${fontKeyName}
				 |   /FirstChar ${firstChar}
				 |   /LastChar ${lastChar}
				 |   /Widths
				 |    [
				 |		 ${withObj.widthList.mkString(" ")}
				 |    ]
				 |   /FontDescriptor ${embeddedDefOpt.get.pdfFontDescriptor.id} 0 R
				 |   /Encoding/WinAnsiEncoding
				 				 |   >>
				 				 |endobj
				 				 |""".stripMargin.getBytes
		}


	}
}


class PdfFontStream(id: Long, val fontName: String, val fontMetric: FontMetric, pdfCompression: Boolean)(implicit itemList: ListBuffer[PdfBaseItem]) extends PdfBaseItem(id) {
	override def content: Array[Byte] = {
		val byteArray = Files.readAllBytes(Paths.get(fontName))
		val byteArray2 = {
			val f = new SyncFileUtil("/home/marian/workspace/GenSNew/good2.pdf", 271, StandardOpenOption.READ)
			val bytes = f.read(8712, None)
			bytes.rewind()
			val nr = bytes.remaining().toInt
			val b1 = new Array[Byte](nr)
			bytes.get(b1)
			b1
		}
		val lg = byteArray.length
		s"""${id} 0 obj
			 			 | <</Length ${lg}/Length1 ${lg}>>stream
			 			 |""".stripMargin.getBytes ++
			byteArray ++
			"\nendstream\nendobj\n".getBytes
		PdfNativeGenerator.writeData(id, byteArray, pdfCompression, true)
	}
}

class PdfFontDescriptor(id: Long, pdfFontStream: PdfFontStream, fontKeyName: String)
                       (implicit itemList: ListBuffer[PdfBaseItem]) extends PdfBaseItem(id) {
	override def content: Array[Byte] = {
		s"""${id} 0 obj
			 |    <</Type/FontDescriptor
			 |    /FontName/${fontKeyName}
			 |    /Flags ${pdfFontStream.fontMetric.fontDescriptor.get.flags}
			 |    /FontBBox[${pdfFontStream.fontMetric.fontDescriptor.get.fontBBox}]
			 |    /ItalicAngle ${pdfFontStream.fontMetric.fontDescriptor.get.italicAngle}
			 |    /Ascent ${pdfFontStream.fontMetric.fontDescriptor.get.ascent}
			 |    /Descent ${pdfFontStream.fontMetric.fontDescriptor.get.descent}
			 |    /CapHeight ${pdfFontStream.fontMetric.fontDescriptor.get.capHeight}
			 |    /StemV 0
			 |    /FontFile2 ${pdfFontStream.id} 0 R
			 |>>
			 |endobj
			 |""".stripMargin.getBytes
	}
}


class PdfPageContent(id: Long, pageItemList: List[PdfPageItem], pdfCompression: Boolean)
                    (implicit itemList: ListBuffer[PdfBaseItem]) extends PdfBaseItem(id) {
	override def content: Array[Byte] = {
		val itemsStr = pageItemList.foldLeft("")((s1, s2) => s1 + "\n" + s2.content)
		PdfNativeGenerator.writeData(id, itemsStr.getBytes, pdfCompression)
	}
}

private abstract class PdfPageItem {
	def content: String
}

private case class PatternDraw(x1: Float, y1: Float, x2: Float, y2: Float, pattern: PdfGPattern)

private case class PdfTxtChuck(x: Float, y: Float, rtext: RText, fontRefName: String, pattern: Option[PatternDraw] = None)


private class PdfText(txtList: List[PdfTxtChuck])
	extends PdfPageItem {
	override def content: String = {
		if (txtList.isEmpty) {
			return ""
		}
		val txtListSimple = txtList.filter(txt => txt.pattern.isEmpty)
		val txtListPattern = txtList.filter(txt => txt.pattern.isDefined)
		val item = txtListSimple.head
		val color = PdfNativeGenerator.convertColor(item.rtext.font.color)
		val firstItemTxt =
			s""" BT /${item.fontRefName} ${item.rtext.font.size} Tf
				 				 |  1 0 0 1 ${item.x.toLong} ${item.y.toLong} Tm
				 				 |  ${color._1} ${color._2} ${color._3} rg
				 				 |        (${item.rtext.txt}) Tj
       """.stripMargin

		val s2 = firstItemTxt + txtListSimple.tail.zipWithIndex.map {
			case (item, i) => {
				val color = PdfNativeGenerator.convertColor(item.rtext.font.color)
				val xRel = txtListSimple(i + 1).x.toLong - txtListSimple(i).x.toLong
				val yRel = txtListSimple(i + 1).y.toLong - txtListSimple(i).y.toLong
				s"""  /${item.fontRefName} ${item.rtext.font.size} Tf
					 					 |  ${xRel} ${yRel} Td
					 					 |  ${color._1} ${color._2} ${color._3} rg
					 					 |  (${item.rtext.txt}) Tj
       """.stripMargin
			}
		}.mkString("")

		// pattern text
		val s3 = if (txtListPattern.isEmpty) ""
		else txtListPattern.map(txt => {
			s""" q
				 				 |/Pattern cs /${item.pattern.get.pattern.name} scn
				 				 |/${item.fontRefName} ${item.rtext.font.size} Tf
				 				 |  1 0 0 1 ${item.x.toLong} ${item.y.toLong} Tm
				 				 |  ${color._1} ${color._2} ${color._3} rg
				 				 |        (${item.rtext.txt}) Tj
				 				 |Q
       """.mkString("")
		})

		s"""${s2}
			 			 |${s3}
			 			 |      ET
       """.stripMargin
	}

}

private class PdfGraphic(items: List[PdfGraphicChuck]) extends PdfPageItem {
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


private class PdfWriter(name: String) {
	new File(name).delete()
	private[this] val writer = new FileOutputStream(name)
	private[render] var position: Long = 0

	def <<(str: String): Unit = {
		<<(str.getBytes)
	}

	def <<<(str: String): Unit = {
		val str1 = str + "\n"
		<<(str1.getBytes)
	}

	def <<(str: Array[Byte]): Unit = {
		writer.write(str)
		position += str.length
	}

	def close(): Unit = {
		writer.flush()
		writer.close()
	}
}

