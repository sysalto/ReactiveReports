package com.sysalto.render.util.fonts.parsers.otf

import com.sysalto.render.util.SyncFileUtil
import com.sysalto.render.util.fonts.parsers.ttf.Common.{TableOffset, TtfTable, Uint16}

class Hmtx(f: SyncFileUtil,tables:Map[String,Long],numOfLongHorMetrics:Int) {

  class LongHorMetric(f: SyncFileUtil) {
    val advanceWidth = new Uint16(f)
    val leftSideBearing = new Uint16(f)
  }
  f.seek(tables.get(("hmtx")).get)
  val hMetrics = (for (i <- 1 to numOfLongHorMetrics) yield new LongHorMetric(f))
}
