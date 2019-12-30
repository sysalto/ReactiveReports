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


package com.sysalto.report.reportTypes


import com.sysalto.report.util.GroupUtilDefs.ReportRecord

import scala.annotation.varargs

class GroupUtil[T,R](groupList: List[Group[T,R]]) {
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
	def instance: GroupUtil.type = this

	@varargs def create[T,R](list: Group[T,R]*): GroupUtil[T,R] = {
		new GroupUtil(list.toList)
	}

	def getRec[T](rec: ReportRecord[T]): T = {
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
