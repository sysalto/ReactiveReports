package com.sysalto.report.function

trait RConsumer1[T] {
	def apply(v1: T): Unit
}
