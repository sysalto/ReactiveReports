package com.sysalto.report.reportTypes

import com.sysalto.report.util.ColumnWidthType

import scala.annotation.varargs
import scala.collection.JavaConverters._

/**
  * Created by marian on 3/4/17.
  */
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


object Row {
  def instance = this

  def apply(left: Float, right: Float, columns: java.util.List[Column]) =
    new Row(left, right, columns.asScala.toList)

  @varargs def apply(left: Float, right: Float, columns: Column*): Row =
    new Row(left, right, columns.toList)

}