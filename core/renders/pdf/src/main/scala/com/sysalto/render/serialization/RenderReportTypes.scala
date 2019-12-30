package com.sysalto.render.serialization

import java.awt.image.BufferedImage
import java.io.{ByteArrayOutputStream, File, FileOutputStream}
import java.net.URL
import java.nio.file.{Files, Paths}
import java.util.zip.Deflater

import com.fasterxml.jackson.annotation.{JsonIgnoreProperties, JsonTypeInfo}
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.sysalto.render.PdfDraw.{DrawPoint, PdfGraphicFragment, roundRectangle}
import com.sysalto.render.basic.PdfBasic
import com.sysalto.render.basic.PdfBasic._
import com.sysalto.render.util.PageTree.PageNode
import com.sysalto.render.util.fonts.parsers.FontParser.FontMetric
import com.sysalto.report.ReportCommon
import com.sysalto.report.ReportTypes.BoundaryRect
import com.sysalto.report.reportTypes.{RFont, ReportColor, ReportTxt}
import com.sysalto.report.util.PersistenceUtil
import javax.imageio.ImageIO

import scala.collection.immutable.HashMap
import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.util.Try
import com.sysalto.report.util.serializers.ObjectSerialization


object RenderReportTypes {
  private[render] val ENCODING = "ISO-8859-1"

  @JsonTypeInfo(use = Id.CLASS, property = "className")
  @JsonIgnoreProperties(Array("persistenceUtil"))
  private[render] abstract class PdfBaseItem(val id: Long)(protected val className: String) {
    var offset: Long = 0
    var persistenceUtil: PersistenceUtil = null

    def content: Array[Byte]

    def write(pdfWriter: PdfWriter)(implicit persistenceUtil: PersistenceUtil): Unit = {
      offset = pdfWriter.position
      setObject(this)
      pdfWriter << content
    }

    override def toString: String = {
      s"[${this.getClass.getTypeName}]\n" + content
    }

  }

  private[render] class PdfDests(id: Long, val dests: ListBuffer[(String, String)] = ListBuffer())
    extends PdfBaseItem(id)("PdfDests") {
    override def content: Array[Byte] = {
      val head = dests.head
      s"""${id} 0 obj
         				 |<</Names[(${head._1}) 2 0 R]>>
         				 |endobj
         				 |""".stripMargin.getBytes(ENCODING)
    }
  }

  @JsonIgnoreProperties(Array("pdfImage", "image", "width", "height", "opacityStr", "persistenceUtil"))
  private[render] class PdfDrawImage(val idPdfImage: Long, val x: Float, val y: Float, val scale: Float = 1,
                                     val opacity: Option[Float] = None) extends PdfGraphicFragment("PdfDrawImage") {
    def content: String = {
      implicit val persistenceUtil1 = persistenceUtil
      val pdfImage = getObject[PdfImage](idPdfImage)
      val image = pdfImage.imageMeta
      val width = image.width * scale
      val height = image.height * scale
      val opacityStr = ""
      s"""q
         				 			 |$opacityStr
         				 			 |$width 0 0 $height ${x} ${y} cm
         				 			 |/${pdfImage.name} Do
         				 			 | Q
    """.stripMargin
    }

  }

  private[render] class PdfNames(id: Long, val idDest: Long)
    extends PdfBaseItem(id)("PdfNames") {
    override def content: Array[Byte] = {
      s"""${id} 0 obj
         				 |<</Dests ${idDest} 0 R>>
         				 |endobj
         				 |""".stripMargin.getBytes(ENCODING)
    }
  }


  class PdfFontStream(id: Long, val fontName: String, var fontMetric: FontMetric, val pdfCompression: Boolean)
    extends PdfBaseItem(id)("PdfFontStream") {
    override def content: Array[Byte] = {
      val byteArray = Files.readAllBytes(Paths.get(fontName))
      val lg = byteArray.length
      s"""${id} 0 obj
         				 			 | <</Length ${lg}/Length1 ${lg}>>stream
         				 			 |""".stripMargin.getBytes(ENCODING) ++
        byteArray ++
        "\nendstream\nendobj\n".getBytes(ENCODING)
      writeData(id, byteArray, pdfCompression, true)
    }
  }


  class PdfFontDescriptor(id: Long, val idPdfFontStream: Long, val idToUnicode: Long, val fontKeyName: String)
    extends PdfBaseItem(id)("PdfFontDescriptor") {
    override def content: Array[Byte] = {
      implicit val persistenceUtil1 = persistenceUtil
      val pdfFontStream: PdfFontStream = getObject[PdfFontStream](idPdfFontStream)
      val fontDescriptor = pdfFontStream.fontMetric.fontDescriptor.get
      val fontFile = if (fontDescriptor.isOtf) "FontFile3" else "FontFile2"
      val style = if (fontDescriptor.isOtf) s"/Style<</Panose<${fontDescriptor.panose}>>>" else ""
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
         |    ${style}
         				 |    /${fontFile} ${pdfFontStream.id} 0 R
         				 |>>
         				 |endobj
         				 |""".stripMargin.getBytes(ENCODING)
    }
  }

  class ToUnicode(id: Long, var glyphNbr: Map[Integer, Int])(implicit persistenceUtil: PersistenceUtil) extends PdfBaseItem(id)("ToUnicode") {
    override def content: Array[Byte] = {
      def fillHexa(str: String) = str.reverse.padTo(4, '0').reverse

      val glyphStr = glyphNbr.map { case (key, value) => s"<${fillHexa(value.toHexString)}><${fillHexa(key.asInstanceOf[Int].toHexString)}>" }.mkString("\n")
      s"""${id} 0 obj
         |<</Length 344>>stream
         |/CIDInit /ProcSet findresource begin
         |12 dict begin
         |begincmap
         |/CIDSystemInfo
         |<< /Registry (Adobe)
         |/Ordering (UCS)
         |/Supplement 0
         |>> def
         |/CMapName /Adobe-Identity-UCS def
         |/CMapType 2 def
         |1 begincodespacerange
         |<0000><FFFF>
         |endcodespacerange
         |${glyphNbr.size} beginbfrange
         |${glyphStr}
         |endbfrange
         |endcmap
         |CMapName currentdict /CMap defineresource pop
         |end end
         |
         |endstream
         |endobj
         |""".stripMargin.getBytes(ENCODING)
    }
  }

  class DescendantFonts(id: Long, val idFontDescriptor: Long, var glyphWidth: Map[Integer, Double])
    extends PdfBaseItem(id)("DescendantFonts") {
    override def content: Array[Byte] = {
      implicit val persistenceUtil1 = persistenceUtil
      val pdfFontDescriptor = getObject[PdfFontDescriptor](idFontDescriptor)
      val glyphWidthStr = glyphWidth.map { case (key, value) => s"$key[${value.toLong}]" }.mkString("")
      s"""${id} 0 obj
         |<<
         |/BaseFont/${pdfFontDescriptor.fontKeyName}
         |/CIDSystemInfo
         |<<
         |/Ordering(Identity)
         |/Registry(Adobe)
         |/Supplement 0
         |>>
         |/FontDescriptor ${idFontDescriptor} 0 R
         |/Subtype/CIDFontType0
         |/Type/Font
         |/W  [${glyphWidthStr}]
         |>>
         |endobj
         |""".stripMargin.getBytes(ENCODING)
    }
  }

  class FontEmbeddedDef(val idPdfFontDescriptor: Long, val idPdfFontStream: Long)


  class PdfFont(id: Long, val refName: String, val font: RFont,
                val embeddedDefOpt: Option[FontEmbeddedDef] = None, val idDescendantFonts: Long = 0)
    extends PdfBaseItem(id)("PdfFont") {
    val charSet = mutable.Set[Char]()

    override def content: Array[Byte] = {
      implicit val persistenceUtil1 = persistenceUtil
      if (embeddedDefOpt.isEmpty) {
        s"""${id} 0 obj
           					 |<<  /Type /Font
           					 |/Subtype /Type1
           					 |/BaseFont /${font.fontKeyName}
           					 |/Encoding /WinAnsiEncoding
           					 |>>
           					 |endobj
           					 |""".stripMargin.getBytes(ENCODING)
      } else {
        val fontEmbedeedDef = embeddedDefOpt.get
        val pdfFontStream = getObject[PdfFontStream](fontEmbedeedDef.idPdfFontStream)
        val pdfFontDescriptor = getObject[PdfFontDescriptor](fontEmbedeedDef.idPdfFontDescriptor)
        val withObj = pdfFontStream.fontMetric.fontDescriptor.get.glyphWidth
        val firstChar = withObj.firstChar
        val lastChar = withObj.lastChar
        if (pdfFontStream.fontMetric.fontDescriptor.get.isOtf) {
          s"""${id} 0 obj
             | << /Type/Font
             |   /Subtype/Type0
             |   /BaseFont/${font.fontKeyName}
             |   /Encoding/Identity-H
             |   /DescendantFonts[${idDescendantFonts} 0 R]
             |   /ToUnicode ${pdfFontDescriptor.idToUnicode} 0 R
             |   >>
             |endobj
             |""".stripMargin.getBytes(ENCODING)
        } else {
          s"""${id} 0 obj
           					 | << /Type/Font
           					 |   /Subtype/TrueType
           					 |   /BaseFont/${font.fontKeyName}
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
           					 				 |""".stripMargin.getBytes(ENCODING)
        }
      }


    }
  }


  private[render] class PdfShaddingFctColor(id: Long, val color1: ReportColor, val color2: ReportColor)
    extends PdfBaseItem(id)("PdfShaddingFctColor") {
    override def content: Array[Byte] = {
      val colorNbr1 = convertColor(color1)
      val colorNbr2 = convertColor(color2)

      s"""${id} 0 obj
         				 			 |  <</FunctionType 2/Domain[0 1]/C0[${colorNbr1._1} ${colorNbr1._2} ${colorNbr1._3}]/C1[${colorNbr2._1} ${colorNbr2._2} ${colorNbr2._3}]/N 1>>
         				 			 |endobj
         				 			 |""".stripMargin.getBytes(ENCODING)
    }
  }

  private[render] class PdfColorShadding(id: Long, val x0: Float, val y0: Float, val x1: Float, val y1: Float, val idPdfShaddingFctColor: Long)
    extends PdfBaseItem(id)("PdfColorShadding") {
    override def content: Array[Byte] = {
      implicit val persistenceUtil1 = persistenceUtil
      val pdfShaddingFctColor = getObject[PdfShaddingFctColor](idPdfShaddingFctColor)
      s"""${id} 0 obj
         				 			 |  <</ShadingType 2/ColorSpace/DeviceRGB/Coords[$x0 $y0  $x1 $y1]/Function ${pdfShaddingFctColor.id} 0 R>>
         				 			 |endobj
         				 			 |""".stripMargin.getBytes(ENCODING)
    }
  }

  class PdfGPattern(id: Long, val idPdfShadding: Long) extends PdfBaseItem(id)("PdfGPattern") {
    val name = "P" + id

    override def content: Array[Byte] = {
      s"""${id} 0 obj
         				 			 |  <</PatternType 2/Shading ${idPdfShadding} 0 R/Matrix[1 0 0 1 0 0]>>
         				 			 |endobj
         				 			 |""".stripMargin.getBytes(ENCODING)
    }
  }

  class PdfRectangle(val x1: Long, val y1: Long, val x2: Long, val y2: Long, val radius: Float, val borderColor: Option[ReportColor],
                     val fillColor: Option[ReportColor], @JsonDeserialize(contentAs = classOf[java.lang.Long]) val idPatternColor: Option[Long] = None)
    extends PdfGraphicFragment("PdfRectangle") {
    override def content: String = {
      implicit val persistenceUtil1 = persistenceUtil
      val patternColor = if (idPatternColor.isDefined) Some(getObject[PdfGPattern](idPatternColor.get)) else None
      val paternStr = if (patternColor.isDefined) {
        pattern(patternColor.get.name)
      } else ""
      val borderStr = if (borderColor.isDefined) border(borderColor.get) else ""
      val fillStr = if (fillColor.isDefined) fill(fillColor.get) else ""
      val operator = fillStroke(fillColor.isDefined || patternColor.isDefined, borderColor.isDefined)
      val rectangleStr = if (radius == 0) rectangle(x1, y1, x2 - x1, y2 - y1) else roundRectangle(x1, y1, x2, y2, radius)
      s"""${saveStatus}
         				 |${paternStr}
         				 |${borderStr}
         				 |${fillStr}
         				 |${rectangleStr}
         				 | ${operator}
         				 |${restoreStatus}
       """.stripMargin.trim
    }

  }


  abstract class PdfAnnotation(id: Long)(override val className: String) extends PdfBaseItem(id)(className)


  class ImageMetaData(val width: Int, val height: Int, val imageInByte: Array[Byte], val pixelSize: Int)

  object ImageMeta {
    def getNewFile(fileName: String): ImageMetaData = {
      val file =
        if (fileName.startsWith("http://") || fileName.startsWith("https://")) {
          val url = new URL(fileName)
          val img1 = ImageIO.read(url)
          val tempFile = File.createTempFile("rap", ".jpg")
          tempFile.deleteOnExit()
          ImageIO.write(img1, "jpg", tempFile)
          tempFile
        } else {
          new File(fileName)
        }
      val bimg: BufferedImage =
        ImageIO.read(file)
      val width: Int = bimg.getWidth()
      val height: Int = bimg.getHeight()
      val size: Long = file.length
      val baos = new ByteArrayOutputStream()
      ImageIO.write(bimg, "jpg", baos)
      baos.flush()
      val imageInByte: Array[Byte] = baos.toByteArray
      baos.close()
      val pixelSize: Int = bimg.getColorModel.getComponentSize(0)
      new ImageMetaData(width, height, imageInByte, pixelSize)
    }
  }


  class PdfImage(id: Long, val fileName: String) extends PdfBaseItem(id)("PdfImage") {
    val name = "img" + id
    val imageMeta = ImageStore.getNewImageMeta(fileName) //new ImageMeta(fileName)


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
         				 			 |""".stripMargin.getBytes(ENCODING) ++
        imageMeta.imageInByte ++
        "\nendstream\nendobj\n".getBytes(ENCODING)
    }

  }

  @JsonTypeInfo(use = Id.CLASS, property = "className")
  @JsonIgnoreProperties(Array("persistenceUtil"))
  private[render] abstract class PdfPageItem(protected val className: String) {
    var persistenceUtil: PersistenceUtil = null

    def content: String
  }

  class PdfPageContent(id: Long, val pageItemList: List[PdfPageItem], val pdfCompression: Boolean)
    extends PdfBaseItem(id)("PdfPageContent") {
    override def content: Array[Byte] = {
      pageItemList.foreach(item => item.persistenceUtil = persistenceUtil)
      val itemsStr = pageItemList.foldLeft("")((s1, s2) => s1 + "\n" + s2.content)
      writeData(id, itemsStr.getBytes(ENCODING), pdfCompression)
    }
  }

  class PdfPage(id: Long, var parentId: Long = 0, var pageWidth: Float, var pageHeight: Float,
                var idFontList: List[java.lang.Long] = List(), var idPdfPatternList: List[java.lang.Long] = List(),
                var idAnnotationList: List[java.lang.Long] = List(),
                var idImageList: mutable.Set[java.lang.Long] = mutable.HashSet(), var idContentPageOpt: Option[Long] = None)
    extends PdfBaseItem(id)("PdfPage") with PageNode {

    override def addChild(child: PageNode): Unit = {}


    override def content: Array[Byte] = {
      implicit val persistenceUtil1: PersistenceUtil = persistenceUtil

      val contentStr = if (idContentPageOpt.isDefined) s"/Contents ${idContentPageOpt.get} 0 R" else ""
      val fontStr = "/Font<<" + idFontList.map(idFont => {
        val font = getObject[PdfFont](idFont)
        s"/${font.refName} ${font.id} 0 R"
      }).mkString("") + ">>"
      val patternStr = if (idPdfPatternList.isEmpty) "" else "/Pattern <<" +
        idPdfPatternList.map(idItem => {
          val item = getObject[PdfGPattern](idItem)
          s"/${item.name} ${item.id} 0 R"
        }).mkString(" ") + ">>"
      val imageStr = if (idImageList.isEmpty) "" else "/XObject <<" +
        idImageList.map(idItem => {
          val item = getObject[PdfImage](idItem)
          s"/${item.name} ${item.id} 0 R"
        }).mkString(" ") + ">>"
      val annotsStr = if (idAnnotationList.isEmpty) "" else "/Annots [" +
        idAnnotationList.map(idItem => {
          val item = getObject[PdfAnnotation](idItem)
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
      result.replaceAll("(?m)^\\s+\\n", "").getBytes(ENCODING)
    }
  }


  private[render] class PdfPageList(id: Long, var parentId: Option[Long] = None, var pageList: ListBuffer[java.lang.Long] = ListBuffer())
                                   (implicit persistenceUtil: PersistenceUtil) extends PdfBaseItem(id)("PdfPageList") with PageNode {

    override def addChild(child: PageNode): Unit = {
      child match {
        case pdfPageList: PdfPageList => {
          pdfPageList.parentId = Some(this.id)
          pageList += pdfPageList.id
          leafNbr += child.leafNbr
          setObject(pdfPageList)
        }
        case pdfPage: PdfPage => {
          pageList += pdfPage.id
          pdfPage.parentId = id
          leafNbr += 1
          setObject(pdfPage)
        }
      }
      setObject(this)
    }

    override def content: Array[Byte] = {
      val parentIdStr = if (parentId.isDefined) s"/Parent ${parentId.get} 0 R" else ""
      val pageListStr = pageList.map(id => s"${id} 0 R").mkString("\n")
      s"""${id} 0 obj
         				 			 |  <<  /Type /Pages ${parentIdStr}
         				 			 |      /Kids [ ${pageListStr} ]
         				 			 |      /Count ${leafNbr}
         				 			 |  >>
         				 			 |endobj
         				 			 |""".stripMargin.getBytes(ENCODING)
    }
  }

  private[serialization] class PdfCatalog(id: Long, var idPdfPageListOpt: Option[Long] = None, var idPdfNamesOpt: Option[Long] = None)
                                         (implicit persistenceUtil: PersistenceUtil) extends PdfBaseItem(id)("PdfCatalog") {
    override def content: Array[Byte] = {
      val namesStr = if (idPdfNamesOpt.isEmpty) "" else s"/Names ${idPdfNamesOpt.get} 0 R"
      s"""${id} 0 obj
         				 |<<  /Type /Catalog
         				 |    /Pages ${idPdfPageListOpt.get} 0 R
         				 |    ${namesStr}
         				 |>>
         				 |endobj
         				 |""".stripMargin.getBytes(ENCODING)
    }
  }

  private[render] class PdfTxtFragment(val x: Float, val y: Float, val rtext: ReportTxt, val fontRefName: String,
                                       val patternOpt: Option[PatternDraw] = None) extends Serializable

  private[serialization] class PdfText(val txtList: List[PdfTxtFragment], val fontMap: Map[String, PdfFont])
    extends PdfPageItem("PdfText") {

    private[this] def escapeText(input: String): String = {
      val s1 = input.replace("\\", "\\\\")
      val s2 = s1.replace("(", "\\(")
      s2.replace(")", "\\)")
    }

    def getText(txt: PdfTxtFragment): String = {
      implicit val persistenceUtil1 = persistenceUtil
      val isOtf = txt.rtext.font.externalFont.isDefined &&
        txt.rtext.font.externalFont.get.regular.endsWith(".otf")
      if (isOtf) {
        val fontName = txt.rtext.font.externalFont.get.name
        val pdfFont = fontMap(txt.rtext.font.fontKeyName)
        val pdfFontStream = getObject[PdfFontStream](pdfFont.embeddedDefOpt.get.idPdfFontStream)
        val fontMetric = pdfFontStream.fontMetric
        val txtStr = escapeText(txt.rtext.txt).map(char => {
          val charInt = char.toInt.asInstanceOf[Integer]
          if (!fontMetric.fontGlyphNbr.contains(charInt)) {
            throw new Exception(s"Char:$char from ${txt.rtext.txt} is not in fontGlyphNbr")
          }
          val nbr = fontMetric.fontGlyphNbr(charInt)
          nbr.toHexString.reverse.padTo(4, '0').reverse
        }).mkString
        s"<${txtStr}>"
      } else {
        val txtStr = escapeText(txt.rtext.txt)
        s"(${txtStr})"
      }

    }

    override def content: String = {
      if (txtList.isEmpty) {
        return ""
      }
      implicit val persistenceUtil1 = persistenceUtil
      val txtListSimple = txtList.filter(txt => txt.patternOpt.isEmpty)
      val txtListPattern = txtList.filter(txt => txt.patternOpt.isDefined)
      val item = txtListSimple.head
      val color = convertColor(item.rtext.font.color)
      val isOtf = item.rtext.font.externalFont.isDefined &&
        item.rtext.font.externalFont.get.regular.endsWith(".otf")
      val firstItemTxt =
        s""" BT /${item.fontRefName} ${item.rtext.font.size} Tf
           					 				 |  1 0 0 1 ${item.x.toLong} ${item.y.toLong} Tm
           					 				 |  ${color._1} ${color._2} ${color._3} rg
           					 				 |        ${getText(item)} Tj
       """.stripMargin
      //      }

      val s2 = firstItemTxt + txtListSimple.tail.zipWithIndex.map {
        case (item, i) => {
          val color = convertColor(item.rtext.font.color)
          val xRel = txtListSimple(i + 1).x.toLong - txtListSimple(i).x.toLong
          val yRel = txtListSimple(i + 1).y.toLong - txtListSimple(i).y.toLong
          s"""  /${item.fontRefName} ${item.rtext.font.size} Tf
             						 					 |  ${xRel} ${yRel} Td
             						 					 |  ${color._1} ${color._2} ${color._3} rg
             						 					 |   ${getText(item)} Tj
       """.stripMargin
        }
      }.mkString("")

      // pattern text
      val s3 = if (txtListPattern.isEmpty) ""
      else txtListPattern.map(txt => {
        val pattern = getObject[PdfGPattern](item.patternOpt.get.idPattern)
        s""" q
           					 				 |/Pattern cs /${pattern.name} scn
           					 				 |/${item.fontRefName} ${item.rtext.font.size} Tf
           					 				 |  1 0 0 1 ${item.x.toLong} ${item.y.toLong} Tm
           					 				 |  ${color._1} ${color._2} ${color._3} rg
           					 				 |         ${getText(item)} Tj
           					 				 |Q
       """.mkString("")
      })

      s"""${s2}
         				 			 |${s3}
         				 			 |      ET
       """.stripMargin
    }

  }

  private[render] class PatternDraw(val x1: Float, val y1: Float, val x2: Float, val y2: Float, val idPattern: Long)


  abstract class PdfAction(id: Long)(override protected val className: String) extends PdfBaseItem(id)(className)


  class PdfGoToUrl(id: Long, val url: String) extends PdfAction(id)("PdfGoToUrl") {
    override def content: Array[Byte] = {
      s"""${id} 0 obj
         				 |<<
         				 |  /Type /Action
         				 |  /S /URI
         				 |  /IsMap false
         				 |  /URI(${url})
         				 |>>
         				 |endobj
         				 |""".stripMargin.getBytes(ENCODING)
    }
  }


  private[render] class PdfLink(id: Long, val boundaryRect: BoundaryRect, val idAction: Long)
    extends PdfAnnotation(id)("PdfLink") {
    override def content: Array[Byte] = {
      s"""${id} 0 obj
         				 |  << /Type /Annot
         				 |  /Subtype /Link
         				 |  /Rect [${boundaryRect}]
         				 |  /F 4
         				 |  /Border [ 0 0 0 ]
         				 |  /A ${idAction} 0 R
         				 |>>
         				 |endobj
         				 |""".stripMargin.getBytes(ENCODING)
    }
  }

  class PdfGoToPage(id: Long, val pageNbr: Long, val left: Int, val top: Int)
    extends PdfAction(id)("PdfGoToPage") {
    override def content: Array[Byte] = {
      s"""${id} 0 obj
         				 |<<
         				 |  /Type /Action
         				 |  /S /GoTo
         				 |  /D [ ${pageNbr - 1} /Fit ]
         				 |>>
         				 |endobj
         				 |""".stripMargin.getBytes(ENCODING)
    }
  }

  private[serialization] class PdfGraphic(val items: List[PdfGraphicFragment])
    extends PdfPageItem("PdfGraphic") {
    override def content: String = {
      val str = items.map(item => {
        item.persistenceUtil = persistenceUtil
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


  class DirectDrawMovePoint(val x: Float, val y: Float) extends PdfGraphicFragment("DirectDrawMovePoint") {
    override def content: String = {
      s"""${x} ${y} m \n"""
    }
  }

  class DirectDrawLine(val x: Float, val y: Float) extends PdfGraphicFragment("DirectDrawLine") {
    override def content: String = {
      s"""${x} ${y} l \n"""
    }
  }


  class DirectDrawCircle(val x: Float, val y: Float, val radius: Float) extends PdfGraphicFragment("DirectDrawCircle") {
    override def content: String = PdfBasic.circle(new DrawPoint(x, y), radius)
  }

  class DirectDrawArc(val x: Float, val y: Float, val radius: Float, val startAngle: Float, val endAngle: Float)
    extends PdfGraphicFragment("DirectDrawArc") {
    override def content: String = {
      val p0 = new DrawPoint((x + radius * Math.cos(startAngle)).toFloat, (y + radius * Math.sin(startAngle)).toFloat)
      val lg = radius * 4 / 3.0 * Math.tan((endAngle - startAngle) * 0.25)
      val p1 = new DrawPoint((p0.x - lg * Math.sin(startAngle)).toFloat, (p0.y + lg * Math.cos(startAngle)).toFloat)
      val p3 = new DrawPoint((x + radius * Math.cos(endAngle)).toFloat, (y + radius * Math.sin(endAngle)).toFloat)
      val p2 = new DrawPoint((p3.x + lg * Math.sin(endAngle)).toFloat, (p3.y - lg * Math.cos(endAngle)).toFloat)
      s"""${p1.x} ${p1.y} ${p2.x} ${p2.y} ${p3.x} ${p3.y} c \n"""
    }
  }


  class DirectDrawStroke(val reportColor: ReportColor) extends PdfGraphicFragment("DirectDrawStroke") {
    override def content: String = {
      val color = ReportColor.convertColor(reportColor)
      s"${color._1} ${color._2} ${color._3} RG\n"
    }
  }

  class DirectDrawFill(val reportColor: ReportColor) extends PdfGraphicFragment("DirectDrawFill") {
    override def content: String = {
      val color = ReportColor.convertColor(reportColor)
      s"${color._1} ${color._2} ${color._3} rg\n"
    }
  }

  class DirectDrawClosePath() extends PdfGraphicFragment("DirectDrawClosePath") {
    override def content: String = "h\n"
  }

  class DirectDraw(val code: String) extends PdfGraphicFragment("DirectDraw") {
    override def content: String = code
  }


  class DirectFillStroke(val fill: Boolean, val stroke: Boolean) extends PdfGraphicFragment("DirectFillStroke") {
    override def content: String = {
      (fill, stroke) match {
        case (true, true) => "B\n"
        case (true, false) => "f\n"
        case (false, true) => "S\n"
        case _ => ""
      }
    }
  }

  class DirectSaveStatus() extends PdfGraphicFragment("DirectSaveStatus") {
    override def content: String = {
      "q\n"
    }
  }

  class DirectRestoreStatus() extends PdfGraphicFragment("DirectRestoreStatus") {
    override def content: String = {
      "Q\n"
    }
  }


  class DirectColorBorder(borderColor: ReportColor) extends PdfGraphicFragment("DirectColorBorder") {
    override def content: String = {
      val color = ReportColor.convertColor(borderColor)
      s"${color._1} ${color._2} ${color._3} RG\n"
    }
  }

  class DirectColorFill(fillColor: ReportColor) extends PdfGraphicFragment("DirectColorFill") {
    override def content: String = {
      val color = ReportColor.convertColor(fillColor)
      s"${color._1} ${color._2} ${color._3} rg\n"
    }
  }

  class DirectClosePath() extends PdfGraphicFragment("DirectClosePath") {
    override def content: String = {
      "h\n"
    }
  }

  class DirectPattern(patternName: String) extends PdfGraphicFragment("DirectPattern") {
    override def content: String = {
      s"/Pattern cs /${patternName} scn"
    }
  }


  class DirectDrawRectangle(val x1: Float, val y1: Float, val x2: Float, val y2: Float) extends PdfGraphicFragment("DirectDrawRectangle") {
    override def content: String = {
      s"""${x1} ${y1} ${x2 - x1} ${y2 - y1} re \n"""
    }
  }


  private[serialization] class PdfWriter(name: String) {
    new File(name).delete()
    private[this] val writer = new FileOutputStream(name)
    private[render] var position: Long = 0

    def <<(str: String): Unit = {
      <<(str.getBytes(ENCODING))
    }

    def <<<(str: String): Unit = {
      val str1 = str + "\n"
      <<(str1.getBytes(ENCODING))
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
         				 |""".stripMargin.getBytes(ENCODING) ++
        input ++
        s"""
           					 |endstream
           					 |endobj
           					 |""".stripMargin.getBytes(ENCODING)
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
         				 |""".stripMargin.getBytes(ENCODING) ++
        compressTxt ++
        s"""
           					 |endstream
           					 |endobj
           					 |""".stripMargin.getBytes(ENCODING)
    }
    result
  }


  def setObject(obj: PdfBaseItem)(implicit persistenceUtil: PersistenceUtil): Unit = {
    val data = ObjectSerialization.serialize(obj)
    persistenceUtil.writeObject(obj.id, data)
  }

  def getObject[T <: PdfBaseItem](id: Long)(implicit persistenceUtil: PersistenceUtil, manT: Manifest[T]): T = {
    val bytes = persistenceUtil.readObject(id)
    ObjectSerialization.deserialize[T](bytes)
  }

  def getAllItems(implicit persistenceUtil: PersistenceUtil): List[java.lang.Long] = ReportCommon.asScala(persistenceUtil.getAllKeys)

  def close(implicit persistenceUtil: PersistenceUtil): Unit = {
    persistenceUtil.close()
  }


}
