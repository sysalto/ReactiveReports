package com.sysalto.render.util.fonts.parsers.ttf

import com.sysalto.render.util.SyncFileUtil
import com.sysalto.render.util.fonts.parsers.ttf.Common.{TableOffset, TtfTable, Uint16, Uint32}

class Cmap(f: SyncFileUtil, tables: Map[String, TableOffset]) extends TtfTable(f, tables, "cmap") {

  class Encoding(f: SyncFileUtil) {
    val platformID = new Uint16(f)
    val encodingID = new Uint16(f)
    val offset = new Uint32(f)

    override def toString = s"Encoding($platformID, $encodingID)"
  }


  val version = new Uint16(f)
  val numberSubtables = new Uint16(f)

  val encodings = for (i <- 1 to numberSubtables.value) yield new Encoding(f)
  val unicodeEncoding = encodings.filter(encodeItem => encodeItem.platformID.value == 0)
  val microsoftEncoding = encodings.filter(encodeItem => encodeItem.platformID.value == 3)
  val macintoshEncoding = encodings.filter(encodeItem => encodeItem.platformID.value == 1)
//  val encodingItem = if (unicodeEncoding.nonEmpty) unicodeEncoding.head else {
//    if (microsoftEncoding.nonEmpty) microsoftEncoding.head else macintoshEncoding.head
//  }
  val encodingItem=macintoshEncoding.head
  val mapFormatList = {
    f.seek(tableOffset.offset.value + encodingItem.offset.value)
    val format = f.readShort()
    format match {
      case 4 => new CmapFormat4(f)
      case 6 => new CmapFormat6(f)
    }
  }

  val charGlypList: Map[Int, Int] = mapFormatList match {
    case f6: CmapFormat6 => f6.glyphIndexMap.map { case (key, element) => key.toInt -> element.value }
    case f4: CmapFormat4 => {
      val r1 = for (seg <- 0 until f4.segCount) yield {
        for (char <- f4.startCodeList(seg).value to f4.endCodeList(seg).value) yield {
          val offset = f4.idRangeOffsetList(seg).value + char - f4.startCodeList(seg).value
          char -> offset
        }
      }
      r1.flatten.toMap
    }
  }

}

trait CmapFormat

class CmapFormat4(f: SyncFileUtil) extends CmapFormat {
  val length = new Uint16(f)
  val language = new Uint16(f)
  val segCountX2 = new Uint16(f)
  val segCount = segCountX2.value / 2
  val searchRange = new Uint16(f)
  val entrySelector = new Uint16(f)
  val rangeShift = new Uint16(f)
  val endCodeList = for (i <- 1 to segCount) yield new Uint16(f)
  val reservedPad = new Uint16(f)
  val startCodeList = for (i <- 1 to segCount) yield new Uint16(f)
  val idDeltaList = for (i <- 1 to segCount) yield new Uint16(f)
  val idRangeOffsetList = for (i <- 1 to segCount) yield new Uint16(f)
  val rangeList = for (i <- 0 until segCount) yield (startCodeList(i), endCodeList(i))
}

class CmapFormat6(f: SyncFileUtil) extends CmapFormat {
  val length = new Uint16(f)
  val language = new Uint16(f)
  val firstCode = (new Uint16(f)).value.toChar
  val entryCount = new Uint16(f)
  val glyphIndexMap = (for (i <- 0 until entryCount.value) yield {
    (firstCode + i).toChar -> new Uint16(f)
  }).toMap
}

