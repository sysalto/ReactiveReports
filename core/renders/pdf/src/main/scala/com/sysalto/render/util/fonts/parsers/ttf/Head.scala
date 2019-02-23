package com.sysalto.render.util.fonts.parsers.ttf

import com.sysalto.render.util.SyncFileUtil
import com.sysalto.render.util.fonts.parsers.ttf.Common._


class Head(f: SyncFileUtil, tables: Map[String, TableOffset]) extends TtfTable(f, tables, "head") {
  val version = new Uint32(f)
  val fontRevision = new Uint32(f)
  val checkSumAdjustment = new Uint32(f)
  val magicNumber = new Uint32(f)
  val flags = new Uint16(f)
  val unitsPerEm = new Uint16(f)
  val created = new Uint64(f)
  val modified = new Uint64(f)
  val xMin = new Uint16(f)
  val yMin = new Uint16(f)
  val xMax = new Uint16(f)
  val yMax = new Uint16(f)
  val macStyle = new Uint16(f)
  val lowestRecPPEM = new Uint16(f)
  val fontDirectionHint = new Uint16(f)
  val indexToLocFormat = new Uint16(f)
  val glyphDataFormat = new Uint16(f)
  check
}