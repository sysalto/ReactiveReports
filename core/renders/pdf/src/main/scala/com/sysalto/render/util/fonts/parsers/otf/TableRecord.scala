package com.sysalto.render.util.fonts.parsers.otf

import com.sysalto.render.util.SyncFileUtil
import com.sysalto.render.util.fonts.parsers.ttf.Common.{Offset32, Tag, Uint32}

class TableRecord(f: SyncFileUtil) {
  val tableTag = new Tag(f)
  val checkSum = new Uint32(f)
  val offset = new Offset32(f)
  val length = new Uint32(f)
}
