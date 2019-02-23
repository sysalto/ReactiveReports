package com.sysalto.render.util.fonts.parsers.ttf

import java.nio.file.StandardOpenOption

import com.sysalto.render.util.SyncFileUtil
import com.sysalto.render.util.fonts.parsers.FontParser
import com.sysalto.render.util.fonts.parsers.FontParser.{EmbeddedFontDescriptor, FontBBox, FontMetric, GlyphWidth}
import com.sysalto.render.util.fonts.parsers.ttf.Common.{Offset, TableOffset}

class TtfFontParser(fontFile: String) extends FontParser(fontFile) {


  override protected def parseFont(): FontParser.FontMetric = {
    val f = new SyncFileUtil(fontFile, 0, StandardOpenOption.READ)
    val offset = new Offset(f)
    val tables =
      (for (i <- 1 to offset.numTables.value) yield new TableOffset(f)).map(item => item.tag.value -> item).toMap
    val head = new Head(f, tables)
    val hhea = new Hhea(f, tables)
    val hmtx = new Hmtx(f, tables, hhea.numOfLongHorMetrics.value)
    val cMap = new Cmap(f, tables)
    val name = new Name(f, tables)
    val os2 = new Os2(f, tables)
    val post = new Post(f, tables)

    def convertToPdfUnits(number: Int): Short = {
      (number * 1000 / head.unitsPerEm.value).toShort
    }


    val fontBBox = new FontBBox(convertToPdfUnits(head.xMin.value), convertToPdfUnits(head.yMin.value),
      convertToPdfUnits(head.xMax.value), convertToPdfUnits(head.yMax.value))


    def getFontMetric(): FontMetric = {
      val fontName = name.nameRecordList.filter(item => item.nameID.value == 4).head.str
      val l1 = cMap.charGlypList.map { case (char, index) => char -> hmtx.hMetrics(index).advanceWidth.value / 1f / head.unitsPerEm.value }

      val firstChar = 13
      val lastChar = 255
      val l2 = (for (i <- firstChar to lastChar) yield l1.get(i)).
        filter(item => item.isDefined).map(item => (item.get * 1000f).toShort).toList
      val glyphWidth = new GlyphWidth(firstChar.toShort, lastChar.toShort, l2)

      val fontDescriptor = new EmbeddedFontDescriptor(convertToPdfUnits(os2.sTypoAscender.value),
        convertToPdfUnits(os2.sCapHeight.value),
        convertToPdfUnits(os2.sTypoDescender.value), fontBBox, post.italicAngle.value.toShort, 1 << 5, glyphWidth)

      val result = new FontMetric(fontName, l1, (0, 0), Some(fontDescriptor))
      result
    }

    getFontMetric()

  }
}

object TtfFontParser {
  def main(args: Array[String]): Unit = {
    val ttfParser = new TtfFontParser("/home/marian/workspace/ReactiveReports/examples/src/main/scala/example/fonts/roboto/Roboto-Regular.ttf")
    println(ttfParser.getCharWidth(32.toChar))
  }

}