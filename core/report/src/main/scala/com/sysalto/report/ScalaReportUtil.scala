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




package com.sysalto.report

import com.sysalto.report.reportTypes.{RFont, RText}

import scala.language.implicitConversions

trait  ScalaReportUtil {

  implicit class FloatFormatImpl(number: Float) {
    def printFormat(localeCode: String): String = formatNumber(number, localeCode)
  }

  implicit class DoubleFormatImpl(number: Double) {
    def printFormat(localeCode: String): String = formatNumber(number, localeCode)
  }


  implicit def stringToRText(txt: String): RText = RText(txt, RFont(10))


  private[this] def getFormatter(localeCode: String) = {
    val locale = new _root_.java.util.Locale(localeCode)
    _root_.java.text.NumberFormat.getNumberInstance(locale)
  }

  def formatNumber(number: Float, localeCode: String): String = {
    getFormatter(localeCode).format(number)
  }

  def formatNumber(number: Double, localeCode: String): String = {
    getFormatter(localeCode).format(number)
  }


  def formatNumber(number: Int, localeCode: String): String = {
    getFormatter(localeCode).format(number)
  }

  def formatNumber(number: Long, localeCode: String): String = {
    getFormatter(localeCode).format(number)
  }

}

object ScalaReportUtil extends  ScalaReportUtil
