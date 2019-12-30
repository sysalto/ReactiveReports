package com.sysalto.render.util.fonts.parsers.otf

import com.sysalto.render.util.SyncFileUtil
import com.sysalto.render.util.fonts.parsers.ttf.Common._

class Maxp(f: SyncFileUtil, tables:Map[String,Long]) {
  f.seek(tables.get(("maxp")).get)
  val version = new Fixed(f)
  val numGlyphs = new Uint16(f)
}