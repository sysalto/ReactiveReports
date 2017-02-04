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

import com.typesafe.config.ConfigFactory
import akka.actor._
import akka.serialization._

object SerializerUtil {


  private val system = ActorSystem("serializer", ConfigFactory.load("application"))
  private val serialization = SerializationExtension(system)
  private val serializer = serialization.findSerializerFor("")
  system.terminate()

  def write(data: AnyRef): Array[Byte] = serializer.toBinary(data)

  def read[T <: AnyRef](bytes: Array[Byte]): T = serializer.fromBinary(bytes).asInstanceOf[T]


  def test2(): Unit = {
    class Base() {}

    case class C1(i: Int) extends Base
    case class C2(s: String) extends Base

    class C(val list: List[Base], val opt: Option[Base] = None)


    val s = new C(List(C1(1), C2("test"), C1(3)) ++ List.fill(1000)(C2("test")), Some(C2("oo")))
    val b = write(s)
    println(b.length)
    val r = read[C](b)
    println(r)
  }

  def main(args: Array[String]): Unit = {
    test2()

  }

}
