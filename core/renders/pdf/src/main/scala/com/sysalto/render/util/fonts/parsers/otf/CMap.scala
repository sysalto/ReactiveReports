package com.sysalto.render.util.fonts.parsers.otf

import com.sysalto.render.util.SyncFileUtil
import com.sysalto.render.util.fonts.parsers.ttf.Common.{Int16, Offset32, Uint16}

class CMap(f: SyncFileUtil,tables:Map[String,Long]) {
  f.seek(tables.get(("cmap")).get)
  val start = f.getCurrentPos
  val version = new Uint16(f)
  val numTables = new Uint16(f)
  val encodingRecordList = for (i <- 1 to numTables.value) yield new EncodingRecord(f, start)
}

class EncodingRecord(f: SyncFileUtil, start: Long) {
  val platformID = new Uint16(f)
  val encodingID = new Uint16(f)
  val offset = new Offset32(f)
  val pos = f.getCurrentPos
  f.seek(start + offset.value)
  val format = new Uint16(f)
  val subTable: Option[CmapTableFormat] = if (format.value == 4) {
    Some(new CmapTableFormat4(f))
  } else {
    None
  }
  f.seek(pos)

}

abstract class CmapTableFormat(f: SyncFileUtil)

class CmapTableFormat4(f: SyncFileUtil) extends CmapTableFormat(f) {
  val length = new Uint16(f)
  val language = new Uint16(f)
  val segCount = (new Uint16(f)).value/2
  val searchRange = new Uint16(f)
  val entrySelector = new Uint16(f)
  val rangeShift = new Uint16(f)
  val endCode=for (i<-1 to segCount) yield new Uint16(f).value
  val reservedPad = new Uint16(f)
  val startCode=for (i<-1 to segCount) yield new Uint16(f).value
  val idDelta=for (i<-1 to segCount) yield new Int16(f).value
  val idRangeOffset=for (i<-1 to segCount) yield new Uint16(f).value
  val glyphIdArrayPos=f.getCurrentPos
  def getGlympId(charCode:Long):Long={
    var result=0L
    for (i<-0 to segCount-1) {
      if (endCode(i)>=charCode && startCode(i) <= charCode) {
        if (idRangeOffset(i)==0) {
          result= charCode+idDelta(i)
        } else {
          val v1=charCode - startCode(i)+idRangeOffset(i)/2
          val v2=i+v1-segCount
          f.seek(glyphIdArrayPos+v2*2)
          result=new Uint16(f).value

        }
      }
    }
    result
  }
}