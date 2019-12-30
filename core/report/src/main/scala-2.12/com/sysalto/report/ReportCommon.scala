package com.sysalto.report
import scala.collection.JavaConverters._

object ReportCommon {

  def asJava[T](input:List[T]): java.util.List[T] = input.asJava

  def asScala[T](input:java.util.Iterator[T]): Iterator[T] = input.asScala
  def asScala[T](input:java.util.List[T]): List[T] = input.asScala.toList

  def max(input:List[Float]):Float=input.max

  def sortBy[T](input:List[T])(sortFct:T =>Float):List[T]=input.sortBy(item=>sortFct(item))
}