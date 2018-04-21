package com.sysalto.report.util

trait PersistenceUtil {
	def writeObject(key: Long, obj: Array[Byte]): Unit

	def readObject(key: Long): Array[Byte]

	def getAllKeys: java.util.List[java.lang.Long]

	def open(): Unit

	def close(): Unit

}
