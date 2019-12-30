package com.sysalto.render.util.fonts.parsers.otf

import com.sysalto.render.util.SyncFileUtil
import com.sysalto.render.util.fonts.parsers.ttf.Common.{Card16, Card8, OffSize, TtfType, Uint16, Uint32, Uint8}

class CFF (f: SyncFileUtil) {
  val header = new CFF_Header(f)
  val nameIndex = new CFF_Name_index(f)
}

class CFF_Header (f: SyncFileUtil) {
  val major=new Card8(f)
  val minor=new Card8(f)
  val hdrSize=new OffSize(f)
}

class CFF_Name_index (f: SyncFileUtil) {
  val names=new CFF_Index(f)
}

class CFF_Index (f: SyncFileUtil) {
  val count=new Card16(f)
  val offSize=new OffSize(f)
  val offset:Seq[Int] = for (i <- 1 to count.value+1) yield {
    offSize.value match {
      case 1=>new Uint8(f).value
      case 2=>new Uint16(f).value
      case 4=>new Uint32(f).value.toInt
    }
  }
  val offset_pos=f.getCurrentPos
  val data = for (i <- 0 to count.value-1) yield {
    val startOffset=offset(i)-1
    val endOffset=offset(i+1)-1
    println(i)
    val data=f.readBytes(endOffset-startOffset,Some(offset_pos+startOffset))
    data
  }
}