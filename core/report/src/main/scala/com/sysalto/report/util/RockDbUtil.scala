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

import com.sysalto.report.ReportTypes.ReportPage
import com.sysalto.report.serialization.ReportPageSerializer
import org.rocksdb.{Options, RocksDB}


class RockDbUtil(prefix: String, extension: String, dbFolder: String) {

	def write(key: String, page: ReportPage): Unit = db.put(key.getBytes, ReportPageSerializer.write(page))

	def read(key: String): Option[ReportPage] = {
		val bytes = db.get(key.getBytes)
		if (bytes == null) {
			None
		} else {
			Some(ReportPageSerializer.read(bytes))
		}
	}

	def writeObject(key: Long, obj: Serializable): Unit = {
		val fs = new ByteArrayOutputStream()
		val out = new ObjectOutputStream(fs)
		out.writeObject(obj)
		out.flush
		out.close
		fs.close
		db.put(BigInt(key).toByteArray, fs.toByteArray)
	}

	def readObject[T](key: Long): T = {
		val bytes = db.get(BigInt(key).toByteArray)

		val fs = new ByteArrayInputStream(bytes)
		val in = new ObjectInputStream(fs)
		val result=in.readObject()
		in.close
		fs.close
		result.asInstanceOf[T]
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
