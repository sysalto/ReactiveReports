package com.sysalto.render.util.fonts.parsers.ttf

import com.sysalto.render.util.SyncFileUtil
import com.sysalto.render.util.fonts.parsers.ttf.Common.{TableOffset, TtfTable, Uint16}

class Os2(f: SyncFileUtil, tables: Map[String, TableOffset]) extends TtfTable(f, tables, "OS/2") {
  f.seek(tableOffset.offset.value + 68)
  val sTypoAscender =new Uint16(f)
  val sTypoDescender = new Uint16(f)
  f.skipBytes(16)
  val sCapHeight = new Uint16(f)
}
