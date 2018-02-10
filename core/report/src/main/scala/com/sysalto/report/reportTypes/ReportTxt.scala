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

import com.sysalto.report.RFontAttribute

import scala.collection.mutable.ListBuffer

/**
  * Created by marian on 3/4/17.
  */
case class ReportTxt(txt: String, var font: RFont = RFont(10))  {

  def this(txt: String) = {
    this(txt, RFont(10))
  }

  def size(fontSize: Int): ReportTxt = {
    font.size = fontSize
    this
  }

  def color(r: Int, g: Int, b: Int, opacity: Float = 1): ReportTxt = {
    font.color = ReportColor(r, g, b, opacity)
    this
  }

  def color(rColor: ReportColor): ReportTxt = {
    font.color = rColor
    this
  }

  def bold(): ReportTxt = {
    font.attribute = if (font.attribute == RFontAttribute.ITALIC) RFontAttribute.BOLD_ITALIC else RFontAttribute.BOLD
    this
  }

  def italic(): ReportTxt = {
    font.attribute = if (font.attribute == RFontAttribute.BOLD) RFontAttribute.BOLD_ITALIC else RFontAttribute.ITALIC
    this
  }

  def +(other: ReportTxt) = RTextList(ListBuffer(this, other))

  def plus(other: ReportTxt) = RTextList(ListBuffer(this, other))
}
