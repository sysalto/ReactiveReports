package com.sysalto.render.util.fonts.parsers.ttf

import com.sysalto.render.util.SyncFileUtil
import com.sysalto.render.util.fonts.parsers.ttf.Common._

class Glyf(f: SyncFileUtil, tables: Map[String, TableOffset]) extends TtfTable(f, tables, "head") {
  val numberOfContours = new Uint16(f)
  val xMin = new FWord(f)
  val yMin = new FWord(f)
  val xMax = new FWord(f)
  val yMax = new FWord(f)

  class SimpleGlyf {
    val endPtsOfContoursList = for (i <- 1 to numberOfContours.value) yield new Uint16(f)
    val instructionLength=new Uint16(f)
    val instructionsList=for (i <- 1 to instructionLength.value) yield new Uint8(f)
    val flags=new Uint8(f)
    // TODO: to be continued
  }

  class CompositeGlyf {
    val flags = new Uint16(f)
    val glyphIndex = new Uint16(f)
    // TODO: to be continued
  }
}
