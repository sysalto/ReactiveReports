package com.sysalto.render.util.fonts.parsers.ttf

import java.nio.ByteBuffer

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

  def unsignedLong(a: Long): Long = {
    val prefix: Array[Byte] = Array(0)
    BigInt(prefix ++ BigInt(a).toByteArray).toLong
  }

  def unsignedInt(a: Int): Long = {
    val prefix: Array[Byte] = Array(0)
    BigInt(prefix ++ BigInt(a).toByteArray).toLong
  }

  def unsignedShort(a: Short): Int = {
    val prefix: Array[Byte] = Array(0)
    BigInt(prefix ++ BigInt(a).toByteArray).toInt
  }

  class Int8(f: SyncFileUtil) extends TtfType {
    override type A = Int

    override val value: Int = f.readByte()
  }

  class Uint8(f: SyncFileUtil) extends TtfType {
    override type A = Int

    override val value: Int = {
      val byteBuffer = f.readBytes(1)
      val s = Integer.toBinaryString((byteBuffer.head & 0xFF) + 0x100).substring(1)
      Integer.parseInt(s, 2)
    }
  }

  class Int16(f: SyncFileUtil) extends TtfType {
    override type A = Short

    override val value: Short = f.readShort()
  }

  class Uint16(f: SyncFileUtil) extends TtfType {
    override type A = Int

    override val value: Int = {
      val byteBuffer = f.readBytes(2)
      val s1 = Integer.toBinaryString((byteBuffer(0) & 0xFF) + 0x100).substring(1)
      val s2 = Integer.toBinaryString((byteBuffer(1) & 0xFF) + 0x100).substring(1)
      val s = s1 + s2
      Integer.parseInt(s, 2)
    }
  }


  class Int32(f: SyncFileUtil) extends TtfType {
    override type A = Int

    override val value: Int = f.readInt()
  }

  class Uint32(f: SyncFileUtil) extends TtfType {
    override type A = Long

    override val value: Long = unsignedInt(f.readInt())
  }

  class Int64(f: SyncFileUtil) extends TtfType {
    override type A = Long

    override val value: Long = f.readLong()
  }

  class Uint64(f: SyncFileUtil) extends TtfType {
    override type A = Long

    override val value: Long = unsignedLong(f.readLong())
  }

  class Uint32String(f: SyncFileUtil) extends TtfType {
    override type A = String

    override val value: String = f.readString(4)
  }


  class Tag(f: SyncFileUtil) extends TtfType {
    override type A = String

    override val value = (for (i <- 1 to 4) yield new Uint8(f).value.toChar).mkString
  }

  type FWord = Int16
  type UfWord = Uint16
  type Offset32 = Uint32
  type Fixed = Int32
  type LongDateTime = Uint64
  type OffSize = Uint8
  type Card8 = Uint8
  type Card16 = Uint16
}