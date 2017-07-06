/*
 * ReactiveReports - Free Java /Scala Reporting Library.
 * Copyright (C) 2017 SysAlto Corporation. All rights reserved.
  *
 * Unless you have purchased a commercial license agreement from SysAlto
 * the following license terms apply:
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