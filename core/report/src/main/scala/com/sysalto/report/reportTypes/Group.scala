package com.sysalto.report.reportTypes

/**
  * Created by marian on 3/4/17.
  */
case class Group[T](name: String, get: (T) => Any)
