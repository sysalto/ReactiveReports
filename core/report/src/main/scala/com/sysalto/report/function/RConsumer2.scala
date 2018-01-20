package com.sysalto.report.function

//trait RConsumer2[@specialized(scala.Int, scala.Long, scala.Float, scala.Double) -T1, @specialized(scala.Int, scala.Long, scala.Float, scala.Double) -T2]  {
//	def apply(v1: T1,v2:T2): Unit
//}


trait RConsumer2[T1,T2]  {
	def apply(v1: T1,v2:T2): Unit
}