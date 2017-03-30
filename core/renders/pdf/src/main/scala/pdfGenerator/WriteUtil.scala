package pdfGenerator

import java.io.File
import java.nio.ByteBuffer
import java.nio.channels.AsynchronousFileChannel
import java.nio.file.{StandardOpenOption, Paths}

/**
  * Created by marian on 1/13/16.
  */
case class WriteUtil(fileName: String) {
  (new File(fileName)).delete
  val path = Paths.get(fileName)
  val channel = AsynchronousFileChannel.open(path, StandardOpenOption.WRITE, StandardOpenOption.CREATE)
  var position = 0L;

  def print(str: String) = {
    channel.write(ByteBuffer.wrap(str.getBytes()), position)
    position = position + str.length
    position
  }

  def print(str: Array[Byte]) = {
    channel.write(ByteBuffer.wrap(str), position)
    position = position + str.length
    position
  }

  def println(str: String) = {
    val str1 = str + "\n"
//    System.out.println(str)
    channel.write(ByteBuffer.wrap(str1.getBytes()), position).get
    position = position + str1.length
    position
  }

  def <<(str: String) = print(str)

  def <<(str: Array[Byte]) = print(str)

  def <<<(str: String) = println(str)

  def close(): Unit = {
    channel.close
  }
}
