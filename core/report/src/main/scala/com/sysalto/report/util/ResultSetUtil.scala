package com.sysalto.report.util

import java.sql.ResultSet

trait ResultSetUtil {
	type RecordMap = Map[String, AnyRef]

	implicit class ResultsetToMap(rs: ResultSet) {
		def toMap: RecordMap = {
			val meta = rs.getMetaData
			(for (i <- 1 to meta.getColumnCount) yield {
				meta.getColumnName(i) -> rs.getObject(i)
			}).toMap
		}
	}

	implicit class ResultSetToGroup(rs: ResultSet) {
		def toGroup: ResultSetGroup = ResultSetGroup(rs)
	}

	implicit class GetValueRecord(rec: RecordMap) {
		def value(key: String): AnyRef = rec(key.toUpperCase())
	}


	case class ResultSetGroup(rs: ResultSet) {
		private val meta = rs.getMetaData
		private val columnList = for (i <- 1 to meta.getColumnCount) yield meta.getColumnName(i)
		private var prevRecord: Option[RecordMap] = None
		private var currentRecord: Option[RecordMap] = None
		private var nextRecord: Option[RecordMap] = None

		def getRecord: RecordMap = {
			columnList.map(column => column -> rs.getObject(column)).toMap
		}

		//		def next(): Boolean = {
		//			if (currentRecord.nonEmpty) {
		//				prevRecord = currentRecord
		//				if (nextRecord.isEmpty) {
		//					nextRecord = getRecord
		//					return nextRecord.nonEmpty
		//				}
		//			} else {
		//				currentRecord = getRecord
		//				if (currentRecord.isEmpty) {
		//					return false
		//				}
		//				nextRecord = getRecord
		//				return nextRecord.nonEmpty
		//			}
		//			if (nextRecord.isEmpty) {
		//				currentRecord = getRecord
		//				currentRecord.nonEmpty
		//			} else {
		//				currentRecord = nextRecord
		//				nextRecord = getRecord
		//				nextRecord.nonEmpty
		//			}
		//		}

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


//		def foreach1(call: ((Option[RecordMap], Option[RecordMap], Option[RecordMap])) => Unit): Unit = {
//			while (next()) {
//				call((prevRecord, currentRecord, nextRecord))
//			}
//		}


	}

}

