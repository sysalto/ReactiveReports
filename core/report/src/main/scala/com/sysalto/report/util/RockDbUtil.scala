/*
 * ReactiveReports - Free Java /Scala Reporting Library.
 * Copyright (C) 2017 SysAlto Corporation. All rights reserved.
 *
 * This program is part of ReactiveReports.
 *
 * ReactiveReports is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ReactiveReports is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY. Without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with ReactiveReports.
 * If not, see https://www.gnu.org/licenses/lgpl-3.0.en.html.
 */


package com.sysalto.report.util

import java.io._

import org.rocksdb.{Options, ReadOptions, RocksDB}
import scala.collection.JavaConverters._

import scala.collection.mutable.ListBuffer


class RockDbUtil(prefix: String, extension: String, dbFolder: String) extends PersistenceUtil {
	private[this] val options = new Options().setCreateIfMissing(true)
	private[this] val file = File.createTempFile(prefix, extension, new File(dbFolder))
	private[this] var db: RocksDB = null

	override def writeObject(key: Long, obj: Array[Byte]): Unit = {
		db.put(BigInt(key).toByteArray, obj)
	}

	override def readObject(key: Long): Array[Byte] = db.get(BigInt(key).toByteArray)


	override def getAllKeys: java.util.List[java.lang.Long] = {
		val it = db.newIterator(new ReadOptions())
		val result = ListBuffer[Long]()
		it.seekToFirst()
		while (it.isValid) {
			result += BigInt(it.key()).toLong
			it.next()
		}
		result.toList.sortBy(item => item).map(item=>item.asInstanceOf[java.lang.Long]).asJava
	}


	override def close(): Unit = {
		if (db != null) db.close()
		options.close()
		file.listFiles().foreach(fileItem => fileItem.delete())
		file.delete()
	}

	override def open(): Unit = {
		RocksDB.loadLibrary()
		file.delete
		db = RocksDB.open(options, file.getAbsolutePath)
	}


}

