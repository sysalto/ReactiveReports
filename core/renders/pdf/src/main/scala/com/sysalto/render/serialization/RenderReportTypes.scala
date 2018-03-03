package com.sysalto.render.serialization

import java.awt.image.BufferedImage
import java.io.{ByteArrayOutputStream, File}
import java.net.URL
import java.nio.file.{Files, Paths, StandardOpenOption}
import java.util.zip.Deflater

import com.sysalto.render.PdfWriter
import com.sysalto.render.serialization.RenderProto.OptionFontEmbeddedDef_proto
import com.sysalto.render.serialization.RenderReportSerializer.FontEmbeddedDef_protoSerializer
import com.sysalto.render.util.PageTree.PageNode
import com.sysalto.render.util.SyncFileUtil
import com.sysalto.render.util.fonts.parsers.FontParser.FontMetric
import com.sysalto.report.reportTypes.ReportColor
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


	private[render] class PdfNames(id: Long, val dests: PdfDests) extends PdfBaseItem(id) {
		override def content: Array[Byte] = {
			s"""${id} 0 obj
				 |<</Dests ${dests.id} 0 R>>
				 |endobj
				 |""".stripMargin.getBytes(RenderReportTypes.ENCODING)
		}
	}


	class PdfFontStream(id: Long, val fontName: String, val fontMetric: FontMetric, val pdfCompression: Boolean) extends PdfBaseItem(id) {
		override def content: Array[Byte] = {
			val byteArray = Files.readAllBytes(Paths.get(fontName))
			val byteArray2 = {
				val f = new SyncFileUtil("~/workspace/GenSNew/good2.pdf", 271, StandardOpenOption.READ)
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
				 			 |""".stripMargin.getBytes(RenderReportTypes.ENCODING) ++
				byteArray ++
				"\nendstream\nendobj\n".getBytes(RenderReportTypes.ENCODING)
			RenderReportTypes.writeData(id, byteArray, pdfCompression, true)
		}
	}


	class PdfFontDescriptor(id: Long, val pdfFontStream: PdfFontStream, val fontKeyName: String)
		extends PdfBaseItem(id) {
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
				 |""".stripMargin.getBytes(RenderReportTypes.ENCODING)
		}
	}

	case class FontEmbeddedDef(pdfFontDescriptor: PdfFontDescriptor, pdfFontStream: PdfFontStream)


	object OptionFontEmbeddedDef_protoSerializer {
		def write(obj: Option[FontEmbeddedDef]): OptionFontEmbeddedDef_proto = {
			val builder = OptionFontEmbeddedDef_proto.newBuilder()
			builder.setNotNull(obj.isDefined)
			if (obj.isDefined) {
				builder.setValue(FontEmbeddedDef_protoSerializer.write(obj.get))
			}
			builder.build()
		}

		def read(obj: OptionFontEmbeddedDef_proto): Option[FontEmbeddedDef] = {
			if (!obj.getNotNull) {
				None
			} else {
				Some(FontEmbeddedDef_protoSerializer.read(obj.getValue))
			}
		}
	}


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

	private[render] class PdfColorShadding(id: Long, val x0: Float, val y0: Float, val x1: Float, val y1: Float, val pdfShaddingFctColor: PdfShaddingFctColor)
		extends PdfBaseItem(id) {
		override def content: Array[Byte] = {
			s"""${id} 0 obj
				 			 |  <</ShadingType 2/ColorSpace/DeviceRGB/Coords[$x0 $y0  $x1 $y1]/Function ${pdfShaddingFctColor.id} 0 R>>
				 			 |endobj
				 			 |""".stripMargin.getBytes(RenderReportTypes.ENCODING)
		}
	}

	class PdfGPattern(id: Long, val pdfShadding: PdfColorShadding) extends PdfBaseItem(id) {
		val name = "P" + id

		override def content: Array[Byte] = {
			s"""${id} 0 obj
				 			 |  <</PatternType 2/Shading ${pdfShadding.id} 0 R/Matrix[1 0 0 1 0 0]>>
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

	class PdfImage(id: Long, val fileName: String)(implicit itemList: ListBuffer[PdfBaseItem]) extends PdfBaseItem(id) {
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

	class PdfPageContent(id: Long, val pageItemList: List[PdfPageItem], val pdfCompression: Boolean)
	                    (implicit itemList: ListBuffer[PdfBaseItem]) extends PdfBaseItem(id) {
		override def content: Array[Byte] = {
			val itemsStr = pageItemList.foldLeft("")((s1, s2) => s1 + "\n" + s2.content)
			RenderReportTypes.writeData(id, itemsStr.getBytes(RenderReportTypes.ENCODING), pdfCompression)
		}
	}


	class PdfPage(id: Long, var parentId: Long = 0, var pageWidth: Float, var pageHeight: Float,
	              var fontList: List[PdfFont] = List(), var pdfPatternList: List[PdfGPattern] = List(),
	              var annotation: List[PdfAnnotation] = List(),
	              var imageList: ListBuffer[PdfImage] = ListBuffer(), var contentPage: Option[PdfPageContent] = None)
		extends PdfBaseItem(id) with PageNode {

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

	private[render] class PdfCatalog(id: Long, var pdfPageList: Option[PdfPageList] = None, var pdfNames: Option[PdfNames] = None)
		extends PdfBaseItem(id) {
		override def content: Array[Byte] = {
			val namesStr = if (pdfNames.isEmpty) "" else s"/Names ${pdfNames.get.id} 0 R"
			s"""${id} 0 obj
				 |<<  /Type /Catalog
				 |    /Pages ${pdfPageList.get.id} 0 R
				 |    ${namesStr}


				 |

				 |""".stripMargin.getBytes(RenderReportTypes.ENCODING)
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

}
