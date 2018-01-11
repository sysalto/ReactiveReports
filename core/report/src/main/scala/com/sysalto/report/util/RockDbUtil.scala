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

import java.io.File

import com.typesafe.config.ConfigFactory
import org.rocksdb.{Options, RocksDB}


class RockDbUtil(prefix: String, extension: String, dbFolder: String) {

  def write[T <: AnyRef](key: String, value: T): Unit = db.put(key.getBytes, SerializerUtil.write(value))

  def read[T <: AnyRef](key: String): Option[T] = {
    val bytes = db.get(key.getBytes)
    if (bytes == null) {
      None
    } else {
      Some(SerializerUtil.read[T](bytes))
    }
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

  private[this] val persistence = ConfigFactory.load("persistence")
  private[this] val dbFolder = persistence.getString("persistence.folder")
  private[this] val prefix = persistence.getString("persistence.prefix")
  private[this] val extension = persistence.getString("persistence.extension")

  def apply(): RockDbUtil = new RockDbUtil(prefix, extension, dbFolder)



}
