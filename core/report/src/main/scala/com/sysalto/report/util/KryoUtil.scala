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

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}
import java.util.zip.{DeflaterOutputStream, InflaterInputStream}

import com.esotericsoftware.kryo.{Kryo, Serializer}
import com.esotericsoftware.kryo.Kryo.DefaultInstantiatorStrategy
import com.esotericsoftware.kryo.io.{Input, Output}
import com.esotericsoftware.kryo.serializers.JavaSerializer
import com.romix.scala.serialization.kryo._
import org.objenesis.strategy.StdInstantiatorStrategy


object KryoUtil {
  val compress = true
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


  kryo.setInstantiatorStrategy(new DefaultInstantiatorStrategy(new StdInstantiatorStrategy()))


  kryo.register(Class.forName("scala.None$"), new NoneSerializer())
//  kryo.register(classOf[Some[_]], new SomeSerializer(kryo))

  class NoneSerializer extends Serializer[None.type] {
    override def write(kryo: Kryo, output: Output, `object`: None.type): Unit = ()

    override def read(kryo: Kryo, input: Input, `type`: Class[None.type]): None.type = None
  }

//  class SomeSerializer(kryo: Kryo) extends Serializer[Some[_]] {
//    override def write(kryo: Kryo, output: Output, `object`: Some[_]): Unit = kryo.writeClassAndObject(output, `object`)
//
//    override def read(kryo: Kryo, input: Input, `type`: Class[Some[_]]): Some[_] = Some(kryo.readClassAndObject(input))
//  }

  def register(classList: List[Class[_ <:Any]]): Unit = {
    classList.foreach(clazz => kryo.register(clazz))
  }


  def serialize(item: Any): Array[Byte] = {
    if (compress) {
      val byteArrayOutputStream = new ByteArrayOutputStream()
      val deflaterOutputStream = new DeflaterOutputStream(byteArrayOutputStream)
      val output = new Output(deflaterOutputStream)
      kryo.writeClassAndObject(output, item)
      output.close()
      byteArrayOutputStream.toByteArray
    } else {
      val output = new Output(new ByteArrayOutputStream())
      kryo.writeClassAndObject(output, item)
      output.flush()
      output.getBuffer
    }
  }

  def deserialize(buf: Array[Byte]): AnyRef = {
    val input = if (compress) {
      new Input(new InflaterInputStream(new ByteArrayInputStream(buf)))
    }
    else {
      new Input(buf)
    }
    kryo.readClassAndObject(input)
  }


}
