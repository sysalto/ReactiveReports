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

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}
import java.util.zip.{DeflaterOutputStream, InflaterInputStream}

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.Serializer
import com.esotericsoftware.kryo.io.Input
import com.esotericsoftware.kryo.io.Output
import com.romix.scala.serialization.kryo._
import org.objenesis.strategy.StdInstantiatorStrategy

/**
  * Created by marian on 16/02/16.
  */
object KryoUtil {
  val compress = true

  def getKryo: Kryo = {
    val kryo = new Kryo()

    kryo.addDefaultSerializer(classOf[scala.Enumeration#Value], classOf[EnumerationSerializer])
    kryo.register(Class.forName("scala.Enumeration$Val"))
    kryo.register(classOf[scala.Enumeration#Value])

    //		// Serialization of Scala maps like Trees, etc
    kryo.addDefaultSerializer(classOf[scala.collection.Map[_, _]], classOf[ScalaImmutableMapSerializer])
    kryo.addDefaultSerializer(classOf[scala.collection.generic.MapFactory[scala.collection.Map]], classOf[ScalaImmutableMapSerializer])
    //
    //		// Serialization of Scala sets
    kryo.addDefaultSerializer(classOf[scala.collection.Set[_]], classOf[ScalaImmutableSetSerializer])
    kryo.addDefaultSerializer(classOf[scala.collection.generic.SetFactory[scala.collection.Set]], classOf[ScalaImmutableSetSerializer])

    // Serialization of all Traversable Scala collections like Lists, Vectors, etc
    kryo.addDefaultSerializer(classOf[scala.collection.Traversable[_]], classOf[ScalaCollectionSerializer])

    // Support deserialization of classes without no-arg constructors
    kryo.setInstantiatorStrategy(new StdInstantiatorStrategy())
    kryo
  }

  val kryoD: Kryo = getKryo

  def register(classList: Class[_]*): Unit = {
    classList.foreach(clazz => kryoD.register(clazz))
  }

  def serialize(item: Any, kryo: Kryo = kryoD): Array[Byte] = {
    val byteArrayOutputStream = new ByteArrayOutputStream()
    val output = if (compress) {
      val deflaterOutputStream = new DeflaterOutputStream(byteArrayOutputStream)
      new Output(deflaterOutputStream)
    }
    else new Output(byteArrayOutputStream)
    kryo.writeClassAndObject(output, item)
    output.close()
    byteArrayOutputStream.toByteArray
  }


  def deserialize[T <: AnyRef](buf: Array[Byte], kryo: Kryo = kryoD): T = {
    val input = if (compress) {
      new Input(new InflaterInputStream(new ByteArrayInputStream(buf)))
    }
    else new Input(buf)
    kryo.readClassAndObject(input).asInstanceOf[T]
  }

  object WeekDay extends Enumeration {
    type WeekDay = Value
    val Mon, Tue, Wed, Thu, Fri, Sat, Sun = Value
  }

  object Time extends Enumeration {
    type Time = Value
    val Second, Minute, Hour, Day, Month, Year = Value
  }

  def main(args: Array[String]) {
    import WeekDay._
    import Time._

    val obuf1 = new Output(new ByteArrayOutputStream())
    // Serialize
    kryoD.writeClassAndObject(obuf1, Tue)
    kryoD.writeClassAndObject(obuf1, Second)
    // Deserialize
    val bytes = obuf1.toBytes
    println("lg:" + bytes.length)
    val ibuf1 = new Input(bytes)
    val enumObjWeekday1 = kryoD.readClassAndObject(ibuf1)
    val enumObjTime1 = kryoD.readClassAndObject(ibuf1)
    println(enumObjWeekday1)
    println(enumObjTime1)
  }

}
