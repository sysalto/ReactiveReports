/*
 *  This file is part of the ReactiveReports project.
 *  Copyright (c) 2017 Sysalto Corporation.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License version 3
 * as published by the Free Software Foundation with the addition of the
 * following permission added to Section 15 as permitted in Section 7(a):
 * FOR ANY PART OF THE COVERED WORK IN WHICH THE COPYRIGHT IS OWNED BY
 * Sysalto. Sysalto DISCLAIMS THE WARRANTY OF NON INFRINGEMENT
 * OF THIRD PARTY RIGHTS.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see https://www.gnu.org/licenses/agpl-3.0.en.html.
 */

package com.sysalto.report.util

import scala.collection.mutable.ListBuffer

trait ReportColumnUtil {

  object ColumnWidthType extends Enumeration {
    type Types = Value
    val Fixed, Range, Flex = Value
  }

  case class RMargin(left: Float, right: Float)

  case class RangeWidth(minWidth: Float, maxWidth: Float = Float.MaxValue)

  case class Flex(value: Int)

  case class Column(id: String, columnWidthType: ColumnWidthType.Types, fixedWidth: Option[Float], floatWidth: Option[RangeWidth] = None,
                    flexWidth: Option[Int] = None)

  object Column {
    def apply(id: String, fixedWidth: Float) = new Column(id, ColumnWidthType.Fixed, Some(fixedWidth))

    def apply(id: String, rangeWidth: RangeWidth) = new Column(id, ColumnWidthType.Range, None, Some(rangeWidth))

    def apply(id: String, flex: Flex) =
      new Column(id, ColumnWidthType.Flex, None, None, Some(flex.value))

  }

  case class Row(left: Float, right: Float, columns: List[Column]) {
    private def calculate(): Map[String, RMargin] = {
      val fixColumnList = columns.filter(column => column.columnWidthType == ColumnWidthType.Fixed)
      val fixWidth = fixColumnList.foldLeft(0.0f)((sum, b) => sum + b.fixedWidth.get)
      val flexColumnCount = columns.count(column => column.columnWidthType == ColumnWidthType.Flex)
      val rangeColumnList = columns.filter(column => column.columnWidthType == ColumnWidthType.Range)
      val minWidth = rangeColumnList.foldLeft(0.0f)((sum, b) => sum + b.floatWidth.get.minWidth)
      val flexWidth = right - left - fixWidth - minWidth
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
        val list1 = rangeColumnList.sortBy(
          column => {
            val floatWidth = column.floatWidth.get
            floatWidth.maxWidth / floatWidth.minWidth
          }
        )
        val rangeWidth = if (maxWidth < right - left - fixWidth) maxWidth else right - left - fixWidth

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
          leftMargin += width
          column.id -> RMargin(leftColumnMargin, leftColumnMargin + width)
        }
      ).toMap
    }

    private lazy val columnsBounds = calculate()
    private lazy val columnNameList = columnsBounds.keySet.toList.sortBy(name => name)


    def getColumnBound(columnName: String): RMargin = {
      if (!columnNameList.contains(columnName)) {
        throw new Exception("Column list:" + columnNameList + " doesn't contains " + columnName)
      }
      columnsBounds(columnName)
    }
  }


}

object ReportColumnUtil extends ReportColumnUtil