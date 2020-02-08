package com.sysalto.render.util

import java.nio.{Buffer, ByteBuffer}
import java.nio.channels.{AsynchronousFileChannel, FileChannel}
import java.nio.file.{Paths, StandardOpenOption}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}


class SyncFileUtil(fileName: String, offset: Long, options: StandardOpenOption*) {
	private[this] val fileChannel = FileChannel.open(Paths.get(fileName), options: _*)
	private[this] var currentPos: Long = offset

	def close(): Unit = {
		fileChannel.close()
	}


	def skipBytes(size: Long): Unit = {
		currentPos += size
	}

	def seek(pos: Long): Unit = {
		currentPos = pos + offset
	}


	def read(size: Int, offsetPos: Option[Long]): ByteBuffer = {
		val buffer = ByteBuffer.allocate(size)
		val pos = if (offsetPos.isEmpty) currentPos else offsetPos.get + offset
		fileChannel.read(buffer, pos)
		if (offsetPos.isEmpty) {
			currentPos += size
		}
		buffer
	}

	def readShort(offset: Option[Long] = None): Short = read(2, offset).getShort(0)

	def readByte(offset: Option[Long] = None): Short = read(1, offset).get(0).toShort


	def readInt(offset: Option[Long] = None): Int = read(4, offset).getInt(0)

	def readLong(offset: Option[Long] = None): Long = read(8, offset).getInt(0)


	def readString(size: Int, offset: Option[Long] = None): String = {
		val bytes = read(size, offset).asInstanceOf[Buffer]
		bytes.rewind()
		val l = for (i <- 1 to size) yield bytes.asInstanceOf[ByteBuffer].get.toChar
		l.mkString("")
	}

	def readUnicodeString(size: Int, offset: Option[Long] = None): String = {
		val bytes = read(size, offset).asInstanceOf[Buffer]
		bytes.rewind()
		val l = for (i <- 1 to size / 2) yield bytes.asInstanceOf[ByteBuffer].getChar
		l.mkString("")
	}

	def readBytes(size: Int, offset: Option[Long] = None): Seq[Byte] = {
		val bytes = read(size, offset).asInstanceOf[Buffer]
		bytes.rewind()
		for (i <- 1 to size) yield bytes.asInstanceOf[ByteBuffer].get
	}

	def getCurrentPos=this.currentPos
}


