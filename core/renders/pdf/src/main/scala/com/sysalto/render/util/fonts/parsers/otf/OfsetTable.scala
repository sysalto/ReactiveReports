package com.sysalto.render.util.fonts.parsers.otf

import com.sysalto.render.util.SyncFileUtil
import com.sysalto.render.util.fonts.parsers.ttf.Common.{Uint16, Uint32}

class OfsetTable(f: SyncFileUtil) {
  val sfntVersion = new Uint32(f)
  val numTables = new Uint16(f)
  val searchRange = new Uint16(f)
  val entrySelector = new Uint16(f)
  val rangeShift = new Uint16(f)
}