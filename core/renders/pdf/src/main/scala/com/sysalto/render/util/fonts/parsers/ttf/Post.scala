package com.sysalto.render.util.fonts.parsers.ttf

import com.sysalto.render.util.SyncFileUtil
import com.sysalto.render.util.fonts.parsers.ttf.Common.{TableOffset, TtfTable, Uint16}

class Post (f: SyncFileUtil, tables: Map[String, TableOffset]) extends TtfTable(f, tables, "post") {
  f.seek(tableOffset.offset.value + 4)
  val italicAngle =  new Uint16(f)

}
