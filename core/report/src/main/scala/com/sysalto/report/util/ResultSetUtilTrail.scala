package com.sysalto.report.util

import java.sql.ResultSet

trait ResultSetUtilTrail {
	type RecordMap = Map[String, AnyRef]

	implicit class ResultsetToMap(rs: ResultSet) {
		def toMap: RecordMap = ResultSetUtil.toMap(rs)
	}

	implicit class ResultSetToGroup(rs: ResultSet) {
		def toGroup: ResultSetGroup = ResultSetUtil.toGroup(rs)
	}

	implicit class GetValueRecord(rec: RecordMap) {
		def value(key: String): AnyRef = rec(key.toUpperCase())
	}



}

case class ResultSetGroup(rs: ResultSet) {
	type RecordMap = Map[String, AnyRef]
	private val meta = rs.getMetaData
	private val columnList = for (i <- 1 to meta.getColumnCount) yield meta.getColumnName(i)
	private var prevRecord: Option[RecordMap] = None
	private var currentRecord: Option[RecordMap] = None
	private var nextRecord: Option[RecordMap] = None

	def getRecord: RecordMap = {
		columnList.map(column => column -> rs.getObject(column)).toMap
	}


	def foreach(call: ((Option[RecordMap], Option[RecordMap], Option[RecordMap])) => Unit): Unit = {
		while (rs.next()) {
			if (currentRecord.isEmpty) {
				currentRecord = Some(getRecord)
			} else {
				if (nextRecord.nonEmpty) {
					prevRecord = currentRecord
					currentRecord = nextRecord
					nextRecord = Some(getRecord)
					call((prevRecord, currentRecord, nextRecord))
				} else {
					nextRecord = Some(getRecord)
					call((prevRecord, currentRecord, nextRecord))
				}
			}
		}
		prevRecord = currentRecord
		currentRecord = nextRecord
		nextRecord = None
		call((prevRecord, currentRecord, nextRecord))
	}


}


object ResultSetUtil {

	implicit class GetValueRecord(rec: Map[String, AnyRef]) {
		def value(key: String): AnyRef = rec(key.toUpperCase())
	}

	def toMap(rs: ResultSet): Map[String, AnyRef] = {
		val meta = rs.getMetaData
		(for (i <- 1 to meta.getColumnCount) yield {
			meta.getColumnName(i) -> rs.getObject(i)
		}).toMap
	}

	def toGroup(rs:ResultSet): ResultSetGroup = ResultSetGroup(rs)

	def getRecordValue[T](rec: Map[String, AnyRef], field: String): T = {
		(rec value (field)).asInstanceOf[T]
	}

}

