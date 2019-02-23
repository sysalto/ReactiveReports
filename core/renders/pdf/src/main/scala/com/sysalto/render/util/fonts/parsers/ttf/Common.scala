package com.sysalto.render.util.fonts.parsers.ttf

import com.sysalto.render.util.SyncFileUtil

object Common {

  trait TtfType {
    type A

    val value: A

    override def toString: String = value.toString
  }

  class Offset(f: SyncFileUtil) {
    val scalarType = new Uint32(f)
    val numTables = new Uint16(f)
    val searchRange = new Uint16(f)
    val entrySelector = new Uint16(f)
    val rangeShift = new Uint16(f)
  }

  class TableOffset(f: SyncFileUtil) {
    val tag = new Uint32String(f)
    val checkSum = new Uint32(f)
    val offset = new Uint32(f)
    val length = new Uint32(f)
  }

  abstract class TtfTable(f: SyncFileUtil, tables: Map[String, TableOffset], tableName: String) {
    protected val tableOffset = tables(tableName)
    f.seek(tableOffset.offset.value)

    def check = assert(f.getCurrentPos - tableOffset.offset.value == tableOffset.length.value)
  }

  class Uint32(f: SyncFileUtil) extends TtfType {
    override type A = Long

    override val value: Long = f.readInt()
  }

  type Fixed = Uint32

  class Uint64(f: SyncFileUtil) extends TtfType {
    override type A = Long

    override val value: Long = f.readLong()
  }

  class Uint32String(f: SyncFileUtil) extends TtfType {
    override type A = String

    override val value: String = f.readString(4)
  }

  class Uint16(f: SyncFileUtil) extends TtfType {
    override type A = Int

    override val value: Int = f.readShort()
  }

  class Uint8(f: SyncFileUtil) extends TtfType {
    override type A = Int

    override val value: Int = f.readByte()
  }

  type FWord = Uint16
  type UfWord = Uint16

}