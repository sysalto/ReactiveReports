package com.sysalto.report.reportTypes

import scala.collection.mutable.ListBuffer

/**
  * Created by marian on 3/14/17.
  */
case class RTextList(list: ListBuffer[RText]) {
  def +(other: RText): RTextList = {
    list += other
    this
  }
}