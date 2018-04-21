package com.sysalto.report.util


trait PersistenceFactory {
	def open(): PersistenceUtil = {
		val db = getPersistence()
		db.open()
		db
	}

	def getPersistence(): PersistenceUtil
}
