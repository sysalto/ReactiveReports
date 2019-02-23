package com.sysalto.render.util.fonts.parsers.ttf

import com.sysalto.render.util.SyncFileUtil
import com.sysalto.render.util.fonts.parsers.ttf.Common.{TableOffset, TtfTable, Uint16, Uint32}

class Name(f: SyncFileUtil, tables: Map[String, TableOffset]) extends TtfTable(f, tables, "name") {
  val format = new Uint16(f)
  val count = new Uint16(f)
  val stringOffset = new Uint16(f)
  val nameRecordList = for (i <- 1 to count.value) yield new NameRecord(f)
  val langTagCount = new Uint16(f)
  val langTagRecord = for (i <- 1 to langTagCount.value) yield new LangTagRecord(f)


  class NameRecord(f: SyncFileUtil) {
    val platformID = new Uint16(f)
    val encodingID = new Uint16(f)
    val languageID = new Uint16(f)
    val nameID = new Uint16(f)
    val length = new Uint16(f)
    val offset = new Uint16(f)
    val isUnicodeStr = platformID == 0 || platformID == 3 || platformID == 2 && encodingID == 1
    val soffset = Some(tableOffset.offset.value + stringOffset.value + offset.value)
    val str = if (isUnicodeStr) {
      f.readUnicodeString(length.value, soffset)
    }
    else {
      f.readString(length.value, soffset)
    }
  }

  class LangTagRecord(f: SyncFileUtil) {
    val length = new Uint16(f)
    val offset = new Uint16(f)
  }

}