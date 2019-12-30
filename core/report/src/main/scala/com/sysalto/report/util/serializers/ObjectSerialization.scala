package com.sysalto.report.util.serializers

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import com.fasterxml.jackson.module.scala.{DefaultScalaModule, ScalaObjectMapper}

object ObjectSerialization {
  val cborFactory = new CBORFactory()
  val mapper = new ObjectMapper(cborFactory) with ScalaObjectMapper
  mapper.registerModule(DefaultScalaModule)

  def serialize(obj:Any):Array[Byte]= mapper.writeValueAsBytes(obj)

  def deserialize[T](bytes:Array[Byte])(implicit manT: Manifest[T]):T= mapper.readValue[T](bytes)
}
