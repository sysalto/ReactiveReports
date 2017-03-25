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


  private def getFormatter(localeCode: String) = {
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