package util

import java.nio.ByteBuffer
import java.nio.channels.{AsynchronousFileChannel, FileChannel}
import java.nio.file.{Paths, StandardOpenOption}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

class AsyncFileUtil(fileName: String, options: StandardOpenOption*) {
	val fileChannel = AsynchronousFileChannel.open(Paths.get(fileName), options: _*)

	def close(): Unit = {
		fileChannel.close()
	}

	def read(buffer: ByteBuffer, offset: Long): Future[Int] = Future {
		fileChannel.read(buffer, offset).get
	}

	def read(size: Int, offset: Long): Future[ByteBuffer] = Future {
		val buffer = ByteBuffer.allocate(size)
		fileChannel.read(buffer, offset).get
		buffer
	}

	def readShort(offset: Long): Future[Short] = {
		read(2, offset).map(bytes => bytes.getShort(0))
	}

	def readInt(offset: Long): Future[Int] = {
		read(4, offset).map(bytes => bytes.getInt(0))
	}

	def readString(size: Int, offset: Long): Future[String] = {
		read(size, offset).map(bytes => {
			bytes.rewind()
			val l = for (i <- 1 to size) yield bytes.get.toChar
			l.mkString("")
		})
	}
}


class SyncFileUtil(fileName: String, options: StandardOpenOption*) {
	val fileChannel = FileChannel.open(Paths.get(fileName), options: _*)
	private var currentPos: Long = 0

	def close(): Unit = {
		fileChannel.close()
	}


	def skip(size: Long): Unit = {
		currentPos += size
	}

	def seek(size: Long): Unit = {
		currentPos = size
	}


	def read(size: Int, offset: Option[Long]): ByteBuffer = {
		val buffer = ByteBuffer.allocate(size)
		fileChannel.read(buffer, offset.getOrElse(currentPos))
		if (offset.isEmpty) {
			currentPos += size
		}
		buffer
	}

	def readShort(offset: Option[Long] = None): Short = read(2, offset).getShort(0)


	def readInt(offset: Option[Long] = None): Int = read(4, offset).getInt(0)


	def readString(size: Int, offset: Option[Long] = None): String = {
		val bytes = read(size, offset)
		bytes.rewind()
		val l = for (i <- 1 to size) yield bytes.get.toChar
		l.mkString("")
	}

	def readUnicodeString(size: Int, offset: Option[Long] = None): String = {
		val bytes = read(size, offset)
		bytes.rewind()
		val l = for (i <- 1 to size/2) yield bytes.getChar
		l.mkString("")
	}
}

object AsyncFileUtil {


	def test(): Unit = {
		val myFile = "/home/marian/transfer/tahoma.ttf"
		val fileChannel = AsynchronousFileChannel.open(Paths.get(myFile), StandardOpenOption.READ)
		import java.nio.ByteBuffer
		val buffer = ByteBuffer.allocate(100)
		val operation = fileChannel.read(buffer, 0)
		val op = Future {
			operation.get
		}
		Await.result(op, Duration.Inf)
		val arr = buffer.array()
		println("OK")
	}

	def test1(): Unit = {
		val f = new AsyncFileUtil("/home/marian/transfer/tahoma.ttf", StandardOpenOption.READ)
		val buffer = ByteBuffer.allocate(100)
		val op = f.read(buffer, 0)
		Await.result(op, Duration.Inf)
		val arr = buffer.array()
		println("OK")
	}

	//  def test2(): Unit = {
	//    val f = new AsyncFileUtil("/home/marian/transfer/tahoma.ttf", StandardOpenOption.READ)
	//    val op = f.read(100, Some(0))
	//    val res = Await.result(op, Duration.Inf)
	//    println("OK")
	//  }

	def main(args: Array[String]): Unit = {
		//    test2()
	}

}
