package com.sysalto.render.serialization

import java.awt.image.BufferedImage
import java.io.{ByteArrayOutputStream, File, FileOutputStream}
import java.net.URL
import java.nio.file.{Files, Paths, StandardOpenOption}
import java.util.zip.Deflater

import com.sysalto.render.PdfDraw.PdfGraphicFragment
import com.sysalto.render.util.PageTree.PageNode
import com.sysalto.render.util.SyncFileUtil
import com.sysalto.render.util.fonts.parsers.FontParser.FontMetric
import com.sysalto.report.reportTypes.{ReportColor, ReportTxt}
import javax.imageio.ImageIO

import scala.collection.mutable.ListBuffer

private[render] object RenderReportTypes {
	private[render] val ENCODING = "ISO-8859-1"

	private[render] abstract class PdfBaseItem(val id: Long) {
		var offset: Long = 0

		def content: Array[Byte]

		def write(pdfWriter: PdfWriter): Unit = {
			offset = pdfWriter.position
			pdfWriter << content
		}

		override def toString: String = {
			s"[${this.getClass.getTypeName}]\n" + content
		}
	}

	private[render] class PdfDests(id: Long, val dests: ListBuffer[(String, String)] = ListBuffer()) extends PdfBaseItem(id) {
		override def content: Array[Byte] = {
			val head = dests.head
			s"""${id} 0 obj
				 |<</Names[(${head._1}) 2 0 R]>>
				 |endobj
				 |""".stripMargin.getBytes(RenderReportTypes.ENCODING)
		}
	}


	private[render] class PdfNames(id: Long, val idDest: Long) extends PdfBaseItem(id) {
		override def content: Array[Byte] = {
			s"""${id} 0 obj
				 |<</Dests ${idDest} 0 R>>
				 |endobj
				 |""".stripMargin.getBytes(RenderReportTypes.ENCODING)
		}
	}


	class PdfFontStream(id: Long, val fontName: String, val fontMetric: FontMetric, val pdfCompression: Boolean) extends PdfBaseItem(id) {
		override def content: Array[Byte] = {
			val byteArray = Files.readAllBytes(Paths.get(fontName))
			val lg = byteArray.length
			s"""${id} 0 obj
				 			 | <</Length ${lg}/Length1 ${lg}>>stream
				 			 |""".stripMargin.getBytes(RenderReportTypes.ENCODING) ++
				byteArray ++
				"\nendstream\nendobj\n".getBytes(RenderReportTypes.ENCODING)
			RenderReportTypes.writeData(id, byteArray, pdfCompression, true)
		}
	}


	class PdfFontDescriptor(id: Long, val idPdfFontStream: Long, val fontKeyName: String)
		extends PdfBaseItem(id) {
		override def content: Array[Byte] = {
			val pdfFontStream: PdfFontStream = RenderReportTypes.getObject[PdfFontStream](idPdfFontStream)
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
				 |""".stripMargin.getBytes(RenderReportTypes.ENCODING)
		}
	}

	case class FontEmbeddedDef(idPdfFontDescriptor: Long, idPdfFontStream: Long)


	class PdfFont(id: Long, val refName: String, val fontKeyName: String,
	              val embeddedDefOpt: Option[FontEmbeddedDef] = None) extends PdfBaseItem(id) {
		override def content: Array[Byte] = {
			if (embeddedDefOpt.isEmpty) {
				s"""${id} 0 obj
					 |<<  /Type /Font
					 |/Subtype /Type1
					 |/BaseFont /${fontKeyName}
					 |/Encoding /WinAnsiEncoding
					 |>>
					 |endobj
					 |""".stripMargin.getBytes(RenderReportTypes.ENCODING)
			} else {
				val fontEmbedeedDef = embeddedDefOpt.get
				val pdfFontStream = RenderReportTypes.getObject[PdfFontStream](fontEmbedeedDef.idPdfFontStream)
				val pdfFontDescriptor = RenderReportTypes.getObject[PdfFontDescriptor](fontEmbedeedDef.idPdfFontDescriptor)
				val withObj = pdfFontStream.fontMetric.fontDescriptor.get.glyphWidth
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
					 |   /FontDescriptor ${pdfFontDescriptor.id} 0 R
					 |   /Encoding/WinAnsiEncoding
					 				 |   >>
					 				 |endobj
					 				 |""".stripMargin.getBytes(RenderReportTypes.ENCODING)
			}


		}
	}


	private[render] class PdfShaddingFctColor(id: Long, val color1: ReportColor, val color2: ReportColor)
		extends PdfBaseItem(id) {
		override def content: Array[Byte] = {
			val colorNbr1 = RenderReportTypes.convertColor(color1)
			val colorNbr2 = RenderReportTypes.convertColor(color2)

			s"""${id} 0 obj
				 			 |  <</FunctionType 2/Domain[0 1]/C0[${colorNbr1._1} ${colorNbr1._2} ${colorNbr1._3}]/C1[${colorNbr2._1} ${colorNbr2._2} ${colorNbr2._3}]/N 1>>
				 			 |endobj
				 			 |""".stripMargin.getBytes(RenderReportTypes.ENCODING)
		}
	}

	private[render] class PdfColorShadding(id: Long, val x0: Float, val y0: Float, val x1: Float, val y1: Float, val idPdfShaddingFctColor: Long)
		extends PdfBaseItem(id) {
		override def content: Array[Byte] = {
			val pdfShaddingFctColor = RenderReportTypes.getObject[PdfShaddingFctColor](idPdfShaddingFctColor)
			s"""${id} 0 obj
				 			 |  <</ShadingType 2/ColorSpace/DeviceRGB/Coords[$x0 $y0  $x1 $y1]/Function ${pdfShaddingFctColor.id} 0 R>>
				 			 |endobj
				 			 |""".stripMargin.getBytes(RenderReportTypes.ENCODING)
		}
	}

	class PdfGPattern(id: Long, val idPdfShadding: Long) extends PdfBaseItem(id) {
		val name = "P" + id

		override def content: Array[Byte] = {
			s"""${id} 0 obj
				 			 |  <</PatternType 2/Shading ${idPdfShadding} 0 R/Matrix[1 0 0 1 0 0]>>
				 			 |endobj
				 			 |""".stripMargin.getBytes(RenderReportTypes.ENCODING)
		}
	}

	abstract class PdfAnnotation(id: Long) extends PdfBaseItem(id)

	class ImageMeta(fileName: String) {
		val file = if (fileName.startsWith("http://") || fileName.startsWith("https://")) {
			val url = new URL(fileName)
			val img1 = ImageIO.read(url)
			val tempFile = File.createTempFile("rap", ".jpg")
			tempFile.deleteOnExit()
			ImageIO.write(img1, "jpg", tempFile)
			tempFile
		} else {
			new File(fileName)
		}
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

	class PdfImage(id: Long, val fileName: String) extends PdfBaseItem(id) {
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
				 			 |""".stripMargin.getBytes(RenderReportTypes.ENCODING) ++
				imageMeta.imageInByte ++
				"\nendstream\nendobj\n".getBytes(RenderReportTypes.ENCODING)
		}
	}

	private[render] abstract class PdfPageItem {
		def content: String
	}

	class PdfPageContent(id: Long, pageItemList: List[PdfPageItem], pdfCompression: Boolean)
	                    extends PdfBaseItem(id) {
		override def content: Array[Byte] = {
			val itemsStr = pageItemList.foldLeft("")((s1, s2) => s1 + "\n" + s2.content)
			RenderReportTypes.writeData(id, itemsStr.getBytes(RenderReportTypes.ENCODING), pdfCompression)
		}
	}


	class PdfPage(id: Long, var parentId: Long = 0, var pageWidth: Float, var pageHeight: Float,
	              var idFontList: List[Long] = List(), var idPdfPatternList: List[Long] = List(),
	              var idAnnotationList: List[Long] = List(),
	              var idImageList: ListBuffer[Long] = ListBuffer(), var idContentPageOpt: Option[Long] = None)
		extends PdfBaseItem(id) with PageNode {

		override def addChild(child: PageNode): Unit = {}

		override def content: Array[Byte] = {
			val contentStr = if (idContentPageOpt.isDefined) s"/Contents ${idContentPageOpt} 0 R" else ""
			val fontStr = "/Font<<" + idFontList.map(idFont => {
				val font = RenderReportTypes.getObject[PdfFont](idFont)
				s"/${font.refName} ${font.id} 0 R"
			}).mkString("") + ">>"
			val patternStr = if (idPdfPatternList.isEmpty) "" else "/Pattern <<" +
				idPdfPatternList.map(idItem => {
					val item = RenderReportTypes.getObject[PdfGPattern](idItem)
					s"/${item.name} ${item.id} 0 R"
				}).mkString(" ") + ">>"
			val imageStr = if (idImageList.isEmpty) "" else "/XObject <<" +
				idImageList.map(idItem => {
					val item = RenderReportTypes.getObject[PdfImage](idItem)
					s"/${item.name} ${item.id} 0 R"
				}).mkString(" ") + ">>"
			val annotsStr = if (idAnnotationList.isEmpty) "" else "/Annots [" +
				idAnnotationList.map(idItem => {
					val item = RenderReportTypes.getObject[PdfAnnotation](idItem)
					s"${item.id} 0 R"
				}).mkString(" ") + "]"
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
			result.replaceAll("(?m)^\\s+\\n", "").getBytes(RenderReportTypes.ENCODING)
		}
	}


	private[render] class PdfPageList(id: Long, var parentId: Option[Long] = None, var pageList: ListBuffer[Long] = ListBuffer())
		extends PdfBaseItem(id) with PageNode {

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
				 			 |""".stripMargin.getBytes(RenderReportTypes.ENCODING)
		}
	}

	private[serialization] class PdfCatalog(id: Long, var idPdfPageListOpt: Option[Long] = None, var idPdfNamesOpt: Option[Long] = None)
		extends PdfBaseItem(id) {
		override def content: Array[Byte] = {
			val namesStr = if (idPdfNamesOpt.isEmpty) "" else s"/Names ${idPdfNamesOpt.get} 0 R"
			s"""${id} 0 obj
				 |<<  /Type /Catalog
				 |    /Pages ${idPdfPageListOpt.get} 0 R
				 |    ${namesStr}
				 |>>
				 |""".stripMargin.getBytes(RenderReportTypes.ENCODING)
		}
	}

	private[render] case class PdfTxtFragment(x: Float, y: Float, rtext: ReportTxt, fontRefName: String,
	                                          patternOpt: Option[PatternDraw] = None)

	private[serialization] class PdfText(val txtList: List[PdfTxtFragment])
		extends PdfPageItem {

		private[this] def escapeText(input: String): String = {
			val s1 = input.replace("\\", "\\\\")
			val s2 = s1.replace("(", "\\(")
			s2.replace(")", "\\)")
		}

		override def content: String = {
			if (txtList.isEmpty) {
				return ""
			}
			val txtListSimple = txtList.filter(txt => txt.patternOpt.isEmpty)
			val txtListPattern = txtList.filter(txt => txt.patternOpt.isDefined)
			val item = txtListSimple.head
			val color = RenderReportTypes.convertColor(item.rtext.font.color)
			val firstItemTxt =
				s""" BT /${item.fontRefName} ${item.rtext.font.size} Tf
					 				 |  1 0 0 1 ${item.x.toLong} ${item.y.toLong} Tm
					 				 |  ${color._1} ${color._2} ${color._3} rg
					 				 |        (${escapeText(item.rtext.txt)}) Tj
       """.stripMargin

			val s2 = firstItemTxt + txtListSimple.tail.zipWithIndex.map {
				case (item, i) => {
					val color = RenderReportTypes.convertColor(item.rtext.font.color)
					val xRel = txtListSimple(i + 1).x.toLong - txtListSimple(i).x.toLong
					val yRel = txtListSimple(i + 1).y.toLong - txtListSimple(i).y.toLong
					s"""  /${item.fontRefName} ${item.rtext.font.size} Tf
						 					 |  ${xRel} ${yRel} Td
						 					 |  ${color._1} ${color._2} ${color._3} rg
						 					 |  (${escapeText(item.rtext.txt)}) Tj
       """.stripMargin
				}
			}.mkString("")

			// pattern text
			val s3 = if (txtListPattern.isEmpty) ""
			else txtListPattern.map(txt => {
				val pattern = RenderReportTypes.getObject[PdfGPattern](item.patternOpt.get.idPattern)
				s""" q
					 				 |/Pattern cs /${pattern.name} scn
					 				 |/${item.fontRefName} ${item.rtext.font.size} Tf
					 				 |  1 0 0 1 ${item.x.toLong} ${item.y.toLong} Tm
					 				 |  ${color._1} ${color._2} ${color._3} rg
					 				 |        (${escapeText(item.rtext.txt)}) Tj
					 				 |Q
       """.mkString("")
			})

			s"""${s2}
				 			 |${s3}
				 			 |      ET
       """.stripMargin
		}

	}

	private[render] case class PatternDraw(x1: Float, y1: Float, x2: Float, y2: Float, idPattern: Long)

	abstract class PdfAction(id: Long) extends PdfBaseItem(id)


	private[serialization] class PdfGraphic(items: List[PdfGraphicFragment]) extends PdfPageItem {
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


	private[serialization] class PdfWriter(name: String) {
		new File(name).delete()
		private[this] val writer = new FileOutputStream(name)
		private[render] var position: Long = 0

		def <<(str: String): Unit = {
			<<(str.getBytes(RenderReportTypes.ENCODING))
		}

		def <<<(str: String): Unit = {
			val str1 = str + "\n"
			<<(str1.getBytes(RenderReportTypes.ENCODING))
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


	def writeData(id: Long, input: Array[Byte], pdfCompression: Boolean, hasLength1: Boolean = false): Array[Byte] = {
		val length1 = if (hasLength1) s"/Length1 ${input.size}" else ""
		val result = if (!pdfCompression) {
			s"""${id} 0 obj
				 |<</Length ${input.length} ${length1}>>
				 |stream
				 |""".stripMargin.getBytes(RenderReportTypes.ENCODING) ++
				input ++
				s"""
					 |endstream
					 |endobj
					 |""".stripMargin.getBytes(RenderReportTypes.ENCODING)
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
				 |""".stripMargin.getBytes(RenderReportTypes.ENCODING) ++
				compressTxt ++
				s"""
					 |endstream
					 |endobj
					 |""".stripMargin.getBytes(RenderReportTypes.ENCODING)
		}
		result
	}

	def convertColor(color: ReportColor): (Float, Float, Float) = {
		val r = color.r / 255f
		val g = color.g / 255f
		val b = color.b / 255f
		(r, g, b)
	}




	//get object from RockDb
	def getObject[T](id: Long): T = ???

	def setObject[T>:PdfBaseItem](obj:T): Unit = ???
}
