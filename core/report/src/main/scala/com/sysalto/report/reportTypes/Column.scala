package com.sysalto.report.reportTypes

import com.sysalto.report.util.ColumnWidthType
import com.sysalto.report.util.ReportColumnUtil.{Flex, RangeWidth}

/**
  * Created by marian on 3/4/17.
  */
case class Column(id: String, var columnWidthType: ColumnWidthType.Types, var fixedWidth: Option[Float],
                  var floatWidth: Option[RangeWidth] = None, var flexWidth: Option[Int] = None) {
  def flex(width: Int): Column = {
    columnWidthType = ColumnWidthType.Flex
    flexWidth = Some(width)
    this
  }
}

object Column {
  def apply(id: String):Column = apply(id, 0)

  def apply(id: String, fixedWidth: Float) = new Column(id, ColumnWidthType.Fixed, Some(fixedWidth))

  def apply(id: String, rangeWidth: RangeWidth) = new Column(id, ColumnWidthType.Range, None, Some(rangeWidth))

  def apply(id: String, flex: Flex) =
    new Column(id, ColumnWidthType.Flex, None, None, Some(flex.value))

}