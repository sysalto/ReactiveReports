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

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import com.sysalto.report.ReportTypes.ReportPage
import com.sysalto.report.serialization.ReportPageSerializer
import org.rocksdb.{Options, ReadOptions, RocksDB}

import scala.collection.mutable.ListBuffer


class RockDbUtil(prefix: String, extension: String, dbFolder: String) {
	val mapper = new ObjectMapper()
	mapper.registerModule(DefaultScalaModule)

	def write(key: String, page: ReportPage): Unit = db.put(key.getBytes, ReportPageSerializer.write(page))

	def read(key: String): Option[ReportPage] = {
		val bytes = db.get(key.getBytes)
		if (bytes == null) {
			None
		} else {
			Some(ReportPageSerializer.read(bytes))
		}
	}

	def writeObject(key: Long, obj: AnyRef): Unit = {
		val str=mapper.writeValueAsString(obj)
		db.put(BigInt(key).toByteArray, str.getBytes())
	}

	def readObject[T](key: Long): T = {
		val bytes = db.get(BigInt(key).toByteArray)
		val mapper1 = new ObjectMapper() with ScalaObjectMapper
		mapper1.registerModule(DefaultScalaModule)
		mapper1.readValue(new String(bytes))
	}

	def writeObject1(key: Long, obj: Array[Byte]): Unit = {
		val str=mapper.writeValueAsString(obj)
		db.put(BigInt(key).toByteArray, obj)
	}

	def readObject1(key: Long): Array[Byte] = db.get(BigInt(key).toByteArray)


	def convert[T](src: String)(implicit manT: Manifest[T]): T = {
		val a1 = manT.runtimeClass
		val mapper = new ObjectMapper()
		mapper.registerModule(DefaultScalaModule)
		val p2 = mapper.readValue(src, a1)
		println("p2:" + p2)

		// or
		val mapper1 = new ObjectMapper() with ScalaObjectMapper
		mapper1.registerModule(DefaultScalaModule)
		mapper1.readValue[T](src)
	}



	def getAllKeys: List[Long] = {
		val it = db.newIterator(new ReadOptions())
		val result = ListBuffer[Long]()
		it.seekToFirst()
		while (it.isValid) {
			result += BigInt(it.key()).toLong
			it.next()
		}
		result.toList.sortBy(item => item)
	}


	def close(): Unit = {
		if (db != null) db.close()
		options.close()
		file.listFiles().foreach(file => file.delete())
	}

	private[this] val options = new Options().setCreateIfMissing(true)
	private[this] val file = File.createTempFile(prefix, extension, new File(dbFolder))

	RocksDB.loadLibrary()
	file.delete
	private[this] val db = RocksDB.open(options, file.getAbsolutePath)
}

object RockDbUtil {

	private[this] val dbFolder = System.getProperty("java.io.tmpdir")
	private[this] val prefix = "persistence"
	private[this] val extension = ".db"

	def apply(): RockDbUtil = new RockDbUtil(prefix, extension, dbFolder)


}
