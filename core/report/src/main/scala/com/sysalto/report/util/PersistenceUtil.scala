package com.sysalto.report.util

trait PersistenceUtil {
  def writeObject(key: Long, obj: Array[Byte]): Unit

  def writeObject(key: String, obj: Array[Byte]): Unit

  def readObject(key: Long): Array[Byte]

  def readObject(key: String): Array[Byte]

  def getAllKeys: java.util.List[java.lang.Long]

  def open(): Unit

  def close(): Unit

}
