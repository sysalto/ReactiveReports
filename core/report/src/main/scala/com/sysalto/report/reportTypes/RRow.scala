package com.sysalto.report.reportTypes

import com.sysalto.report.{Report, WrapOptions}

import scala.annotation.varargs

/**
  * Created by marian on 3/4/17.
  */
case class RRow(cells: List[RCell]) {
  def calculate(report: Report): Float = {
    val y = report.getY
    val wrapList = cells.map(cell => {
      val result = report.wrap(cell.txt, cell.margin.left, y, cell.margin.right, Float.MaxValue, WrapOptions.LIMIT_TO_BOX, cell.allign, true)
      result.get.currentY
    })
    report.setYPosition(y)
    wrapList.reduceLeft((f1, f2) => if (f1 > f2) f1 else f2)
  }

  def print(report: Report): Unit = {
    val y = report.getY
    cells.foreach(cell => {
      report.wrap(cell.txt, cell.margin.left, y, cell.margin.right, Float.MaxValue, WrapOptions.LIMIT_TO_BOX, cell.allign)
    })
    report.setYPosition(y)
  }

}


object RRow {
  def instance = this

  @varargs def apply(cells: RCell*): RRow = {
    new RRow(cells.toList)
  }
}