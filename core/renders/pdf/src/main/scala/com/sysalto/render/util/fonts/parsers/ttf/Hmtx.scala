package com.sysalto.render.util.fonts.parsers.ttf

import com.sysalto.render.util.SyncFileUtil
import com.sysalto.render.util.fonts.parsers.ttf.Common.{TableOffset, TtfTable, Uint16}

class Hmtx(f: SyncFileUtil, tables: Map[String, TableOffset], numOfLongHorMetrics: Int) extends TtfTable(f, tables, "hmtx") {

  class LongHorMetric(f: SyncFileUtil) {
    val advanceWidth = new Uint16(f)
    val leftSideBearing = new Uint16(f)
  }

  val hMetrics = (for (i <- 1 to numOfLongHorMetrics) yield new LongHorMetric(f)).toList
  //		val leftSideBearing = new FWord(f)
//  check
}
