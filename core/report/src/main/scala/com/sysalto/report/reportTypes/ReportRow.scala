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

import com.sysalto.report.ReportCommon
import com.sysalto.report.util.ColumnWidthType

import scala.annotation.varargs

case class ReportRow(left: Float, right: Float, columns: List[Column]) {
	var cellSpacing = 0f

	private def calculate(): Map[String, ReportMargin] = {
		val fixColumnList = columns.filter(column => column.columnWidthType == ColumnWidthType.Fixed)
		val fixWidth = fixColumnList.foldLeft(0.0f)((sum, b) => sum + b.fixedWidth.get)
		val flexColumnCount = columns.count(column => column.columnWidthType == ColumnWidthType.Flex)
		val rangeColumnList = columns.filter(column => column.columnWidthType == ColumnWidthType.Range)
		val minWidth = rangeColumnList.foldLeft(0.0f)((sum, b) => sum + b.floatWidth.get.minWidth)
		val flexWidth = right - left - fixWidth - minWidth - cellSpacing * (columns.length - 1)
		if (flexWidth <= 0) {
			throw new Exception("flexWidth")
		}
		val mapColumnWidth = if (flexColumnCount > 0) {
			val flexColumnList = columns.filter(column => column.columnWidthType == ColumnWidthType.Flex)
			val totalFlex = flexColumnList.foldLeft(0)((sum, b) => sum + b.flexWidth.get)
			columns.map(column =>
				column.id -> (column.columnWidthType match {
					case ColumnWidthType.Fixed => column.fixedWidth.get
					case ColumnWidthType.Range => column.floatWidth.get.minWidth
					case ColumnWidthType.Flex => flexWidth / totalFlex * column.flexWidth.get
				})
			).toMap
		} else {
			val maxWidth = rangeColumnList.foldLeft(0.0f)((sum, b) => sum + b.floatWidth.get.maxWidth)
			val list1 = ReportCommon.sortBy(rangeColumnList)(
				column => {
					val floatWidth = column.floatWidth.get
					floatWidth.maxWidth / floatWidth.minWidth
				}
			)
			val rangeWidth = if (maxWidth < right - left - fixWidth - cellSpacing * (columns.length - 1))
				maxWidth
			else right - left - fixWidth - cellSpacing * (columns.length - 1)

			var rW = rangeWidth
			val map = list1.zipWithIndex.map {
				case (column, index) =>
					val floatWidth = column.floatWidth.get
					val minWidth = list1.drop(index).foldLeft(0.0f)((sum, b) => sum + b.floatWidth.get.minWidth)

					val w = rW / minWidth * floatWidth.minWidth
					val colW = if (w > floatWidth.maxWidth) floatWidth.maxWidth else w
					rW = rW - colW
					column.id -> colW
			}.toMap

			columns.map(
				column =>
					column.id -> (column.columnWidthType match {
						case ColumnWidthType.Fixed => column.fixedWidth.get
						case ColumnWidthType.Range => map(column.id)
						case ColumnWidthType.Flex => 0
					})
			).toMap
		}
		var leftMargin = left
		columns.map(
			column => {
				val leftColumnMargin = leftMargin
				val width = mapColumnWidth(column.id)
				leftMargin += width+cellSpacing
				column.id -> ReportMargin(leftColumnMargin, leftColumnMargin + width)
			}
		).toMap
	}

	private[this] lazy val columnsBounds = calculate()
	private[this] lazy val columnNameList = columnsBounds.keySet.toList.sortBy(name => name)


	def getColumnBound(columnName: String): ReportMargin = {
		if (!columnNameList.contains(columnName)) {
			throw new Exception("Column list:" + columnNameList + " doesn't contains " + columnName)
		}
		columnsBounds(columnName)
	}

	def setCellSpacing(cellSpacing: Float): ReportRow = {
		this.cellSpacing = cellSpacing
		this
	}
}


object ReportRow {
	def instance: ReportRow.type = this

	def apply(left: Float, right: Float, columns: java.util.List[Column]) =
		new ReportRow(left, right, ReportCommon.asScala(columns))

	@varargs def apply(left: Float, right: Float, columns: Column*): ReportRow =
		new ReportRow(left, right, columns.toList)

}
