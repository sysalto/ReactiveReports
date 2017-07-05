package com.sysalto.report.reportTypes

import com.sysalto.report.util.ResultSetUtil.{RecordMap, ReportRecord}

import scala.annotation.varargs
import scala.collection.JavaConverters._

/**
	* Created by marian on 3/4/17.
	*/

class GroupUtil[T](groupList: List[Group[T]]) {
	@annotation.tailrec
	final def isHeader(name: String, rec: ReportRecord[T]): Boolean = {
		rec match {
			case ReportRecord(prev, crt, _) =>
				if (prev.isEmpty) {
					return true
				}
				val findOpt = groupList.zipWithIndex.find { case (item1, _) => item1.name == name }
				if (findOpt.isEmpty) {
					throw new Exception(s"the group $name not found")
				}
				val (item, idx) = findOpt.get
				val fct = item.get
				if (idx == 0) {
					fct(prev.get) != fct(crt.get)
				} else {
					val result = fct(prev.get) != fct(crt.get)
					if (result) {
						result
					} else {
						isHeader(groupList(idx - 1).name, rec)
					}
				}
		}
	}

	@annotation.tailrec
	final def isFooter(name: String, rec: ReportRecord[T]): Boolean = {
		rec match {
			case ReportRecord(_, crt, next) =>
				if (next.isEmpty) {
					return true
				}
				val findOpt = groupList.zipWithIndex.find { case (item1, _) => item1.name == name }
				if (findOpt.isEmpty) {
					throw new Exception(s"the group $name not found")
				}
				val (item, idx) = findOpt.get
				val fct = item.get
				if (idx == 0) {
					fct(next.get) != fct(crt.get)
				} else {
					val result = fct(next.get) != fct(crt.get)
					if (result) {
						result
					} else {
						isFooter(groupList(idx - 1).name, rec)
					}
				}
		}
	}

}

object GroupUtil {
	def instance = this

	@varargs def apply[T](list: Group[T]*): GroupUtil[T] = {
		new GroupUtil(list.toList)
	}

	def getRec[T](rec:ReportRecord[T]): T = {
		rec match {
			case ReportRecord(_, crt, _) => crt.get
		}
	}


	def isFirstRecord[T](rec: ReportRecord[T]): Boolean = {
		rec.prev.isEmpty
	}

	def isLastRecord[T](rec: ReportRecord[T]): Boolean = {
		rec.next.isEmpty
	}

}