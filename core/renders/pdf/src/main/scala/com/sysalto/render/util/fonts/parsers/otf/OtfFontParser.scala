package com.sysalto.render.util.fonts.parsers.otf

import java.nio.file.StandardOpenOption

import com.sysalto.render.util.SyncFileUtil
import com.sysalto.render.util.fonts.parsers.FontParser
import com.sysalto.render.util.fonts.parsers.FontParser.{EmbeddedFontDescriptor, FontBBox, GlyphWidth}

/*
sudo dnf install fonttools
ttx  -s -d ./otf ./NotoSansCJKjp-Regular.otf

 */

class OtfFontParser(fontFile: String) extends FontParser(fontFile)("OtfFontParser") {
  val f = new SyncFileUtil(fontFile, 0, StandardOpenOption.READ)
  val ofsetTable = new OfsetTable(f)
  val tableRecordList = for (i <- 1 to ofsetTable.numTables.value) yield new TableRecord(f)
  val tablesMap = tableRecordList.map(item => item.tableTag.value -> item.offset.value).toMap
  val hhea = new Hhea(f, tablesMap)
  val head = new Head(f, tablesMap)
  val os2 = new OS2(f, tablesMap)
  val maxp = new Maxp(f, tablesMap)
  val numberOfHMetrics =maxp.numGlyphs.value
  val hmtx = new Hmtx(f, tablesMap, numberOfHMetrics)
  val cmap = new CMap(f, tablesMap)

  var fontGlyphNbrMap = getFontGlyphNbrMap()

  var fontWidth = getFontWidth()
  val fontHeight = (1f, 1f)
  val glyphWidth = new GlyphWidth(0, 255, List[Short]())
  val fontBBox = new FontBBox(0, 0, 1, 1)
  val panose=f"${os2.sFamilyClass(0).value}%02d ${os2.sFamilyClass(1).value}%02d "+os2.panose.map(item=>f"${item.value}%02d").mkString(" ")
  val embeddedFontDescriptor = new EmbeddedFontDescriptor(hhea.ascender.value, os2.sCapHeight.value, hhea.descender.value, fontBBox, 0,
    4, glyphWidth, true,panose)

  def getFontMetric(): FontParser.FontMetric = {
    fontGlyphNbrMap = getFontGlyphNbrMap()
    fontWidth = getFontWidth()
      new FontParser.FontMetric("test", fontWidth, fontGlyphNbrMap.map { case (key, value) => key.asInstanceOf[java.lang.Integer] -> value }, fontHeight, Some(embeddedFontDescriptor))
  }

  private[this] def  getFontGlyphNbrMap()= charList.map(char => {
    val l1 = cmap.encodingRecordList.filter(item => item.subTable.isDefined && item.platformID.value == 0)
    val glyphNbr = if (l1.isEmpty) {
      0
    } else {
      val s1 = l1.head.subTable.get.asInstanceOf[CmapTableFormat4]
      s1.getGlympId(char).toInt
    }
    char -> glyphNbr
  }).toMap

  private[this] def getFontWidth()=fontGlyphNbrMap.map { case (char, glympNbr) => char ->
    (if (hmtx.hMetrics.size > glympNbr)
      hmtx.hMetrics(glympNbr).advanceWidth.value.toFloat
    else 0F)
  }

  override def getCharWidth(char: Char): Float = {
    val l1 = cmap.encodingRecordList.filter(item => item.subTable.isDefined && item.platformID.value == 0)
    val glyphNbr = if (l1.isEmpty) {
      0
    } else {
      val s1 = l1.head.subTable.get.asInstanceOf[CmapTableFormat4]
      s1.getGlympId(char).toInt
    }

    if (hmtx.hMetrics.size > glyphNbr)
      hmtx.hMetrics(glyphNbr).advanceWidth.value.toFloat/head.unitsPerEm.value
    else 0F
  }
}
