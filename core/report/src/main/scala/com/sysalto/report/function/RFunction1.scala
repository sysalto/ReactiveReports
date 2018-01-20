package com.sysalto.report.function

trait RFunction1[T,R] {
	def apply(v1: T): R
}
