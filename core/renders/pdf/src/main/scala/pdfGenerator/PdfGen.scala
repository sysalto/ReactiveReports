package pdfGenerator

import java.io.{ByteArrayOutputStream, File}
import java.util.zip.{Deflater, Inflater}
import javax.imageio.ImageIO

//import core.fonts.afm.FontUtil
//import core.wrap.WordWrap

import scala.collection.mutable.ListBuffer

/**
  * Created by marian on 11/30/16.
  */
object PdfGen {

  class PdfGen(fileName: String, fastWeb: Boolean = false) {
    val writeUtil = WriteUtil(fileName)
    var id = 0l
    val objectList = ListBuffer[Base]()

    private def header(): Unit = {
      writeUtil <<< "%PDF-1.7"
      writeUtil <<< "%\u00a0"
      writeUtil <<< "%"
    }

    def nextId = {
      id = id + 1
      id
    }

    def addToList(obj: Base): Unit = {
      objectList += obj
    }


    def writeAll()(writeCall: () => Unit): Unit = {
      header
      val list = writeCall()
      objectList.foreach(item => item.write())
      writeTrailer()
    }


    def writeTrailer(): Unit = {
      val xrefOffset = writeUtil.position
      writeUtil <<< "xref"
      val objSize = objectList.size + 1
      writeUtil <<< s"0 $objSize"
      writeUtil <<< "0000000000 65535 f "
      objectList.sortBy(obj => obj.objId).foreach(obj => {
        val offset = obj.offset.toString
        val offsetFrmt = "0" * (10 - offset.length) + offset
        writeUtil <<< s"$offsetFrmt 00000 n "
      })
      val catalog = objectList.find(item => item.isInstanceOf[Catalog]).get.asInstanceOf[Catalog]

      writeUtil <<< "trailer"
      writeUtil <<< "<<"
      writeUtil <<< s" /Size $objSize"
      writeUtil <<< s" /Root ${catalog.objId} 0 R"
      writeUtil <<< ">>"
      writeUtil <<< "startxref"
      writeUtil <<< xrefOffset.toString
      writeUtil <<< "%%EOF"
    }

  }

  abstract class Base(pdf: PdfGen, id: Long) extends Serializable {
    val objId = id
    var offset = 0L
    pdf.addToList(this)

    def writeFile(writeCall: () => Unit): Unit = {
      offset = pdf.writeUtil.position
      writeCall()
    }

    def write()
  }

  class Catalog(pdf: PdfGen, id: Long, pageListId: Long) extends Base(pdf, id) {
    def write(): Unit = {
      writeFile { () =>
        pdf.writeUtil <<< s"$id 0 obj"
        pdf.writeUtil <<< "<<"
        pdf.writeUtil <<< " /Type /Catalog"
        pdf.writeUtil <<< s" /Pages ${pageListId} 0 R"
        pdf.writeUtil <<< ">>"
        pdf.writeUtil <<< "endobj"
      }

    }
  }

  class PageList(pdf: PdfGen, id: Long, parent: Option[Long],
                 children: List[Long], pagesSize: Long) extends Base(pdf, id) {
    def write(): Unit = {
      writeFile { () =>
        pdf.writeUtil <<< s"${id} 0 obj"
        pdf.writeUtil <<< "<<"
        pdf.writeUtil <<< " /Type /Pages"
        if (parent != None) {
          pdf.writeUtil <<< s" /Parent ${parent.get} 0 R"
        }
        pdf.writeUtil << " /Kids ["
        children.foreach(item => {
          pdf.writeUtil << s"${item} 0 R "
        })
        pdf.writeUtil <<< "]"
        pdf.writeUtil <<< s" /Count ${pagesSize}"
        pdf.writeUtil <<< ">>"
        pdf.writeUtil <<< "endobj"
      }
    }
  }


  class Page(pdf: PdfGen, id: Long, parentId: Long,
             resourceId: Long, contentId: Long) extends Base(pdf, id) {
    var parent = parentId

    def write(): Unit = {
      writeFile { () =>
        pdf.writeUtil <<< s"${id} 0 obj"
        pdf.writeUtil <<< "<<"
        pdf.writeUtil <<< " /Type /Page"
        pdf.writeUtil <<< s" /Parent ${parent} 0 R"
        pdf.writeUtil <<< " /MediaBox [ 0 0 612 792 ]"
        pdf.writeUtil <<< s" /Resources ${resourceId} 0 R"
        pdf.writeUtil <<< s" /Contents ${contentId} 0 R"
        pdf.writeUtil <<< ">>"
        pdf.writeUtil <<< "endobj"
      }
    }
  }

  trait ResourceElement

  class ResourceOld(pdf: PdfGen, id: Long, fontId: Long,
                    imageList: List[ResourceImage] = List(), opacityIdList: List[Opacity] = List() /*, shadeList: List[Shade] = List()  */)
    extends Base(pdf, id) {
    def write(): Unit = {
      writeFile { () =>
        pdf.writeUtil <<< s"${id} 0 obj"
        pdf.writeUtil <<< "<<"
        pdf.writeUtil <<< " /ProcSet [/PDF /Text]"
        if (fontId > 0) {
          pdf.writeUtil <<< s" /Font <</F1 ${fontId} 0 R >>"
        }
        if (!opacityIdList.isEmpty) {
          pdf.writeUtil <<< s" /ExtGState <<"
          opacityIdList.foreach(opacity => pdf.writeUtil <<< s"/${opacity.name} << /CA ${opacity.opacity} /ca ${opacity.opacity} >>")
          pdf.writeUtil <<< ">>"
        }
        if (!imageList.isEmpty) {
          pdf.writeUtil <<< "/XObject <<"
          imageList.foreach(image => pdf.writeUtil <<< s"/${image.imageName} ${image.id} 0 R ")
          pdf.writeUtil <<< ">>"
        }
        pdf.writeUtil <<< ">>"
        pdf.writeUtil <<< "endobj"
      }
    }
  }


  class Resource(pdf: PdfGen, id: Long, resourceElements: List[ResourceElement])
    extends Base(pdf, id) {
    def write(): Unit = {
      writeFile { () =>
        pdf.writeUtil <<< s"${id} 0 obj"
        pdf.writeUtil <<< "<<"
        pdf.writeUtil <<< " /ProcSet [/PDF /Text]"
        resourceElements.foreach {
          case font: Font => {
            pdf.writeUtil <<< s" /Font <</F1 ${font.objId} 0 R >>"
          }
          case opacity: Opacity => {
            pdf.writeUtil <<< s" /ExtGState <<"
            pdf.writeUtil <<< s"/${opacity.name} << /CA ${opacity.opacity} /ca ${opacity.opacity} >>"
            pdf.writeUtil <<< ">>"
          }
          case image: ResourceImage => {
            pdf.writeUtil <<< "/XObject <<"
            pdf.writeUtil <<< s"/${image.imageName} ${image.id} 0 R "
            pdf.writeUtil <<< ">>"
          }
          case pattern: Pattern => {
            pdf.writeUtil <<< s"/Pattern<</P1 ${pattern.id} 0 R>>"
          }
        }
        pdf.writeUtil <<< ">>"
        pdf.writeUtil <<< "endobj"
      }
    }
  }

  class Font(pdf: PdfGen, id: Long) extends Base(pdf, id) with ResourceElement {
    def write(): Unit = {
      writeFile { () =>
        pdf.writeUtil <<< s"${id} 0 obj"
        pdf.writeUtil <<< "<<"
        pdf.writeUtil <<< " /Type /Font"
        pdf.writeUtil <<< " /Subtype /Type1"
        pdf.writeUtil <<< " /Name /F1"
        pdf.writeUtil <<< " /BaseFont/Helvetica"
        pdf.writeUtil <<< ">>"
        pdf.writeUtil <<< "endobj"
      }
    }
  }

  def writeText(pdf: PdfGen, compress: Boolean, txt: String) {

    if (!compress) {
      pdf.writeUtil <<< s"<</Length ${
        txt.length
      }>>"
      pdf.writeUtil <<< "stream"
      pdf.writeUtil <<< txt
      pdf.writeUtil <<< "endstream"
      pdf.writeUtil <<< "endobj"
    } else {
      val input = txt.getBytes("UTF-8");
      val compresser = new Deflater(Deflater.BEST_COMPRESSION);
      compresser.setInput(input);
      compresser.finish();
      val output = new Array[Byte](input.size)
      val compressedDataLength = compresser.deflate(output);
      compresser.end();
      val compressTxt = output.take(compressedDataLength)
      pdf.writeUtil <<< s"<</Filter/FlateDecode/Length ${
        compressTxt.length
      }>>"
      pdf.writeUtil <<< "stream"
      pdf.writeUtil << compressTxt
      pdf.writeUtil <<< "endstream"
      pdf.writeUtil <<< "endobj"
    }
  }

  class Content(pdf: PdfGen, id: Long, txtList: List[String],
                compress: Boolean = false) extends Base(pdf, id) {
    def write(): Unit = {
      writeFile { () =>
        pdf.writeUtil <<< s"${id} 0 obj"
        val txt = txtList.foldLeft("")((s1, s2) => s1 + s2)
        writeText(pdf, compress, txt)
      }
    }
  }


  abstract class BaseContent() extends Serializable {
    def content: String
  }


  case class Point(x: Int, y: Int)

  case class Rectangle(point: Point, width: Int, height: Int)

  case class Color(r: Double, g: Double, b: Double)

  case class Box(rectangle: Rectangle, lineWidth: Int, marginColor: Color = Color(0, 0, 0))
    extends BaseContent {
    def content: String =
      s"""[ ] 0 d
         |$lineWidth w
         |${marginColor.r} ${marginColor.g} ${marginColor.b} RG
         |${rectangle.point.x} ${rectangle.point.y} ${rectangle.width} ${rectangle.height} re
         |S
      """.stripMargin
  }

  case class BoxFill(rectangle: Rectangle, fillColor: Color, opacity: Option[Opacity]
  = None) extends BaseContent {
    val opacityStr = if (opacity == None) ""

    else s"/${
      opacity.get.
        name
    } gs"

    def content: String =
      s"""[ ] 0 d
         |$opacityStr
         |${
        fillColor.r
      } ${fillColor.g} ${fillColor.b} rg
         |${rectangle.point.x} ${rectangle.point.y}
         ${
        rectangle.width
      }

  ${rectangle.height} re
         |f
      """.stripMargin
  }

  case class Line(lineWidth: Int, point1: Point, point2: Point) extends BaseContent {

    def content: String =
      s"""[ ] 0 d
         |$lineWidth w
         |${point1.x} ${point1.y} m
         |${point2.x} ${point2.y} l
         |S
      """.stripMargin
  }


  case class ResourceImage(pdf: PdfGen, id: Long, image: ImageMeta) extends Base(pdf, id) with ResourceElement {
    val imageName = image.imageName

    def write(): Unit = {
      writeFile { () =>
        pdf.writeUtil <<< s"${id} 0 obj"
        pdf.writeUtil <<< "<<"
        pdf.writeUtil <<< " /Type /XObject"
        pdf.writeUtil <<< " /Subtype /Image"
        pdf.writeUtil <<< s" /Width ${image.width}"
        pdf.writeUtil <<< s" /Height ${image.height}"
        pdf.writeUtil <<< " /ColorSpace /DeviceRGB"
        pdf.writeUtil <<< s" /BitsPerComponent ${image.pixelSize}"
        pdf.writeUtil <<< s" /Length ${image.imageInByte.length}"
        pdf.writeUtil <<< " /Filter /DCTDecode"
        pdf.writeUtil <<< ">>"
        pdf.writeUtil <<< "stream"
        pdf.writeUtil << image.imageInByte
        pdf.writeUtil <<< "endstream"
        pdf.writeUtil <<< "endobj"
      }
    }
  }

  case class DrawImage(point: Point, scale: Double, image: ImageMeta, opacity: Option[Opacity] = None) extends BaseContent {
    val width = image.width * scale
    val height = image.height * scale
    val opacityStr = if (opacity == None) "" else s"/${opacity.get.name} gs"

    def content: String =
      s"""q
         |$opacityStr
         |$width 0 0 $height ${point.x} ${point.y} cm
         |/${image.imageName} Do
         |Q
    """.stripMargin
  }

  case class Opacity(name: String, opacity: Double) extends BaseContent with ResourceElement {
    def content: String = s"/${name} << /CA ${opacity} /ca ${opacity} >>"
  }


  case class FunctionType(pdf: PdfGen, id: Long) extends Base(pdf, id) {
    def write(): Unit = {
      writeFile { () =>
        pdf.writeUtil <<< s"${id} 0 obj"
        pdf.writeUtil <<< "<<"
        pdf.writeUtil <<< "/FunctionType 2/C0[1 1 1]/C1[0 1 0]/Domain[0 1]/N 1"
        pdf.writeUtil <<< ">>"
        pdf.writeUtil <<< "endobj"
      }
    }
  }


  case class ShadingType(pdf: PdfGen, id: Long, functionType: FunctionType) extends Base(pdf, id) {
    def write(): Unit = {
      writeFile { () =>
        pdf.writeUtil <<< s"${id} 0 obj"
        pdf.writeUtil <<< "<<"
        pdf.writeUtil <<< s"/ColorSpace/DeviceRGB/Coords[0 300 0 0]/ShadingType 2/Function ${functionType.id} 0 R/Extend[true true]"
        pdf.writeUtil <<< ">>"
        pdf.writeUtil <<< "endobj"
      }
    }
  }


  case class Pattern(pdf: PdfGen, id: Long, shadingType: ShadingType) extends Base(pdf, id) with ResourceElement {
    def write(): Unit = {
      writeFile { () =>
        pdf.writeUtil <<< s"${id} 0 obj"
        pdf.writeUtil <<< "<<"
        pdf.writeUtil <<< s"/Shading ${shadingType.id} 0 R/Matrix[1 0 0 1 0 0]/PatternType "
        pdf.writeUtil <<< ">>"
        pdf.writeUtil <<< "endobj"
      }
    }
  }


  class ImageMeta(imgName: String, fileName: String) {
    val imageName = imgName
    val file = new File(fileName)
    val bimg = ImageIO.read(file);
    val width = bimg.getWidth();
    val height = bimg.getHeight();
    val size = file.length
    val baos = new ByteArrayOutputStream()
    ImageIO.write(bimg, "jpg", baos)
    baos.flush
    val imageInByte = baos.toByteArray
    baos.close
    val pixelSize = bimg.getColorModel.getComponentSize(0)
  }

  case class Text(x: Int, y: Int, scale: Int, txt: String) extends BaseContent {
    def content =
      s"""BT/F1 1 Tf
         				    |${scale} 0 0 ${scale} $x $y Tm
         				    |($txt)Tj
         				    |ET
         				    |""".stripMargin
  }

  def main(args: Array[String]): Unit = {
    val pdfGen=new PdfGen(("a.txt"))
    val meta=new ImageMeta("img","examples/src/main/resources/images/bank_banner.jpg")
    val img=new ResourceImage(pdfGen,11,meta)
    img.write()

  }

}
