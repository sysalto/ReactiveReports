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

import com.sysalto.report.Report

import scala.annotation.varargs

/**
  * Created by marian on 3/4/17.
  */
case class RRow(cells: List[RCell]) {
  def calculate(report: Report): Float = {
    val y = report.getY
    val wrapList = cells.map(cell => {
      val result = report.wrap(cell.txt, cell.margin.left, y, cell.margin.right, Float.MaxValue,  cell.align, simulate=true)
      result.get.currentY
    })
    report.setYPosition(y)
    wrapList.reduceLeft((f1, f2) => if (f1 > f2) f1 else f2)
  }

  def print(report: Report): Unit = {
    val y = report.getY
    cells.foreach(cell => {
      report.wrap(cell.txt, cell.margin.left, y, cell.margin.right, Float.MaxValue,  cell.align)
    })
    report.setYPosition(y)
  }

}


object RRow {
  def instance: RRow.type = this

  @varargs def apply(cells: RCell*): RRow = {
    new RRow(cells.toList)
  }
}
