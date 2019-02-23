package com.sysalto.render.util.fonts.parsers.ttf

import com.sysalto.render.util.SyncFileUtil
import com.sysalto.render.util.fonts.parsers.ttf.Common._

class Hhea(f: SyncFileUtil, tables: Map[String, TableOffset]) extends TtfTable(f, tables, "hhea") {
  val version = new Fixed(f)
  val ascent = new FWord(f)
  val descent = new FWord(f)
  val lineGap = new FWord(f)
  val advanceWidthMax = new UfWord(f)
  val minLeftSideBearing = new FWord(f)
  val minRightSideBearing = new FWord(f)
  val xMaxExtent = new FWord(f)
  val caretSlopeRise = new Uint16(f)
  val caretSlopeRun = new Uint16(f)
  val caretOffset = new FWord(f)
  val reserved = for (i <- 1 to 4) yield new Uint16(f)
  val metricDataFormat = new Uint16(f)
  val numOfLongHorMetrics = new Uint16(f)
  check
}