package com.sysalto.report.util

import java.sql.ResultSet
import java.util.function.Consumer

import com.sysalto.report.util.ResultSetUtil.ReportRecord

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
	private val reportRecord = ReportRecord[RecordMap](None, None, None)
	//	private var prevRecord: Option[RecordMap] = None
	//	private var currentRecord: Option[RecordMap] = None
	//	private var nextRecord: Option[RecordMap] = None

	def getRecord: RecordMap = {
		columnList.map(column => column -> rs.getObject(column)).toMap
	}


	def foreach(call: ReportRecord[RecordMap] => Unit): Unit = {
		while (rs.next()) {
			if (reportRecord.crt.isEmpty) {
				reportRecord.crt = Some(getRecord)
			} else {
				if (reportRecord.next.nonEmpty) {
					reportRecord.prev = reportRecord.crt
					reportRecord.crt = reportRecord.next
					reportRecord.next = Some(getRecord)
					call(reportRecord)
				} else {
					reportRecord.next = Some(getRecord)
					call(reportRecord)
				}
			}
		}
		reportRecord.prev = reportRecord.crt
		reportRecord.crt = reportRecord.next
		reportRecord.next = None
		call(reportRecord)
	}

	def foreachJ(call: Consumer[ReportRecord[RecordMap]]): Unit = {
		while (rs.next()) {
			if (reportRecord.crt.isEmpty) {
				reportRecord.crt = Some(getRecord)
			} else {
				if (reportRecord.next.nonEmpty) {
					reportRecord.prev = reportRecord.crt
					reportRecord.crt = reportRecord.next
					reportRecord.next = Some(getRecord)
					call.accept(reportRecord)
				} else {
					reportRecord.next = Some(getRecord)
					call.accept(reportRecord)
				}
			}
		}
		reportRecord.prev = reportRecord.crt
		reportRecord.crt = reportRecord.next
		reportRecord.next = None
		call.accept(reportRecord)
	}
}


object ResultSetUtil {
	type RecordMap = Map[String, AnyRef]

	case class ReportRecord[T](var prev: Option[T], var crt: Option[T], var next: Option[T])

	implicit class GetValueRecord(rec: Map[String, AnyRef]) {
		def value(key: String): AnyRef = rec(key.toUpperCase())
	}

	def toMap(rs: ResultSet): Map[String, AnyRef] = {
		val meta = rs.getMetaData
		(for (i <- 1 to meta.getColumnCount) yield {
			meta.getColumnName(i) -> rs.getObject(i)
		}).toMap
	}

	def toGroup(rs: ResultSet): ResultSetGroup = ResultSetGroup(rs)

	def getRecordValue[T](rec: Map[String, AnyRef], field: String): T = {
		(rec value (field)).asInstanceOf[T]
	}

}

