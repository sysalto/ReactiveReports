package com.sysalto.render.util.fonts.parsers.otf

import com.sysalto.render.util.SyncFileUtil
import com.sysalto.render.util.fonts.parsers.ttf.Common._

class Hhea(f: SyncFileUtil,tables:Map[String,Long]) {
  f.seek(tables.get(("hhea")).get)
  val majorVersion = new Uint16(f)
  val minorVersion = new Uint16(f)
  val ascender = new FWord(f)
  val descender = new FWord(f)
  val lineGap = new FWord(f)
  val advanceWidthMax = new UfWord(f)
  val minLeftSideBearing = new FWord(f)
  val minRightSideBearing = new FWord(f)
  val xMaxExtent = new FWord(f)
  val caretSlopeRise = new Int16(f)
  val caretSlopeRun = new Int16(f)
  val caretOffset = new Int16(f)
  val reserved = for (i <- 1 to 4) yield new Int16(f)
  val metricDataFormat = new Int16(f)
  val numberOfHMetrics = new Uint16(f)
}