package com.sysalto.render.util.fonts.parsers.otf

import com.sysalto.render.util.SyncFileUtil
import com.sysalto.render.util.fonts.parsers.ttf.Common.{Fixed, Int16, LongDateTime, Uint16, Uint32}

class Head (f: SyncFileUtil,tables:Map[String,Long]) {
  f.seek(tables.get(("head")).get)
  val majorVersion=new Uint16(f)
  val minorVersion=new Uint16(f)
  val fontRevision=new Fixed(f)
  val checkSumAdjustment=new Uint32(f)
  val magicNumber=new Uint32(f)
  val flags=new Uint16(f)
  val unitsPerEm=new Uint16(f)
  val created=new LongDateTime(f)
  val modified=new LongDateTime(f)
  val xMin=new Int16(f)
  val yMin=new Int16(f)
  val xMax=new Int16(f)
  val yMax=new Int16(f)
  val macStyle=new Uint16(f)
  val lowestRecPPEM=new Uint16(f)
  val fontDirectionHint=new Int16(f)
  val indexToLocFormat=new Int16(f)
  val glyphDataFormat=new Int16(f)
}
