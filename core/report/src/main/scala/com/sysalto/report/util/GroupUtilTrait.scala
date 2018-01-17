/*
 * ReactiveReports - Free Java /Scala Reporting Library.
 * Copyright (C) 2017 SysAlto Corporation. All rights reserved.
 *
 * This program is part of ReactiveReports.
 *
 * ReactiveReports is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ReactiveReports is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY. Without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with ReactiveReports.
 * If not, see https://www.gnu.org/licenses/lgpl-3.0.en.html.
 */



package com.sysalto.report.util

import java.sql.ResultSet
import java.util.function.Consumer

import com.sysalto.report.util.GroupUtilDefs.ReportRecord

trait GroupUtilTrait {
	type RecordMap = Map[String, AnyRef]

	implicit class ResultsetToMap(rs: ResultSet) {
		def toMap: RecordMap = GroupUtilDefs.toMap(rs)
	}

	implicit class ResultSetToGroup(rs: ResultSet) {
		def toGroup: ResultSetGroup = GroupUtilDefs.toGroup(rs)
	}

	implicit class GetValueRecord(rec: RecordMap) {
		def value(key: String): AnyRef = rec(key.toUpperCase())
	}

	implicit class IteratorToGroup[T](iterator: Iterator[T]) {
		def toGroup: IteratorGroup[T] = GroupUtilDefs.toGroup[T](iterator)
	}


}

case class ResultSetGroup(rs: ResultSet) {
	type RecordMap = Map[String, AnyRef]
	private[this] val meta = rs.getMetaData
	private[this] val columnList = for (i <- 1 to meta.getColumnCount) yield meta.getColumnName(i)
	private[this] val reportRecord = ReportRecord[RecordMap](None, None, None)
	//	private[this] var prevRecord: Option[RecordMap] = None
	//	private[this] var currentRecord: Option[RecordMap] = None
	//	private[this] var nextRecord: Option[RecordMap] = None

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

//	def foreachJ(call: Consumer[ReportRecord[RecordMap]]): Unit = {
//		while (rs.next()) {
//			if (reportRecord.crt.isEmpty) {
//				reportRecord.crt = Some(getRecord)
//			} else {
//				if (reportRecord.next.nonEmpty) {
//					reportRecord.prev = reportRecord.crt
//					reportRecord.crt = reportRecord.next
//					reportRecord.next = Some(getRecord)
//					call.accept(reportRecord)
//				} else {
//					reportRecord.next = Some(getRecord)
//					call.accept(reportRecord)
//				}
//			}
//		}
//		reportRecord.prev = reportRecord.crt
//		reportRecord.crt = reportRecord.next
//		reportRecord.next = None
//		call.accept(reportRecord)
//	}
}


case class IteratorGroup[T](iterator: Iterator[T]) {
	private[this] val crtRecord = ReportRecord[T](None, None, None)

	def foreach(call: ReportRecord[T] => Unit): Unit = {
		while (iterator.hasNext) {
			if (crtRecord.crt.isEmpty) {
				crtRecord.crt = Some(iterator.next())
			} else {
				if (crtRecord.next.nonEmpty) {
					crtRecord.prev = crtRecord.crt
					crtRecord.crt = crtRecord.next
					crtRecord.next = Some(iterator.next())
					call(crtRecord)
				} else {
					crtRecord.next = Some(iterator.next())
					call(crtRecord)
				}
			}
		}
		crtRecord.prev = crtRecord.crt
		crtRecord.crt = crtRecord.next
		crtRecord.next = None
		call(crtRecord)
	}
}


object GroupUtilDefs {
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
		(rec value field).asInstanceOf[T]
	}

	def toGroup[T](it:Iterator[T]): IteratorGroup[T] = IteratorGroup[T](it)

}

