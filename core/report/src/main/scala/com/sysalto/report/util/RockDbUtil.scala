/*
 *  This file is part of the ReactiveReports project.
 *  Copyright (c) 2017 Sysalto Corporation.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License version 3
 * as published by the Free Software Foundation with the addition of the
 * following permission added to Section 15 as permitted in Section 7(a):
 * FOR ANY PART OF THE COVERED WORK IN WHICH THE COPYRIGHT IS OWNED BY
 * Sysalto. Sysalto DISCLAIMS THE WARRANTY OF NON INFRINGEMENT
 * OF THIRD PARTY RIGHTS.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see https://www.gnu.org/licenses/agpl-3.0.en.html.
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


  def readOld[T](key: String): Option[T] = {
    val bytes = db.get(key.getBytes)
    if (bytes == null) {
      None
    } else {
      Some(KryoUtil.deserialize(bytes).asInstanceOf[T])
    }
  }

  def close(): Unit = {
    if (db != null) db.close()
    options.close()
    file.listFiles().foreach(file => file.delete())
  }

  private val options = new Options().setCreateIfMissing(true)
  private val file = File.createTempFile(prefix, extension, new File(dbFolder))

  RocksDB.loadLibrary()
  file.delete
  private val db = RocksDB.open(options, file.getAbsolutePath)
}

object RockDbUtil {

  private val persistence = ConfigFactory.load("persistence")
  private val dbFolder = persistence.getString("persistence.folder")
  private val prefix = persistence.getString("persistence.prefix")
  private val extension = persistence.getString("persistence.extension")

  def apply(): RockDbUtil = new RockDbUtil(prefix, extension, dbFolder)

  def test(): Unit = {
    val db = RockDbUtil()
    for (i <- 1 to 30) {
      db.write(s"test$i", s"content$i")
    }

    abstract class M0(name: String) {
      def render()
    }
    case class M1(name: String) extends M0(name) {
      override def render(): Unit = {
        println("this is M1:" + name)
      }
    }
    case class M2(name: String) extends M0(name) {
      override def render(): Unit = {
        println("this is M2:" + name)
      }
    }
    case class Page(items: List[M0])

    val m1 = M1("m1")
    val m2 = M2("m2")
    val m3 = M1("m3")
    val m4 = M2("m4")
    val total1 = Page(List(m1, m2))
    db.write("total1", total1)
    val total2 = Page(List(m3, m4))
    db.write("total2", total2)

    println(db.read("test19"))
    db.read[Page]("total2").get.items.foreach(item => item.render())
    db.read[Page]("total1").get.items.foreach(item => item.render())
    db.close()
  }


  def main(args: Array[String]): Unit = {
    try {
      test()
    } catch {
      case e: Throwable => e.printStackTrace()
    }
  }

}
