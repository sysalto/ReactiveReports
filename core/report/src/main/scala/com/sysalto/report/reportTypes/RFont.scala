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

import com.sysalto.report.RFontAttribute

/**
  * Created by marian on 3/4/17.
  */
case class RFont(var size: Int, var fontName: String = "Helvetica",
                 var attribute: RFontAttribute.Value = RFontAttribute.NORMAL,
                 var color: RColor = RColor(0, 0, 0),var fontFile:Option[String]=None) {
  def fontKeyName: String = fontName +
    (attribute match {
    case RFontAttribute.NORMAL => ""
    case RFontAttribute.BOLD=>"-Bold"
    case RFontAttribute.ITALIC=>"-Oblique"
    case RFontAttribute.BOLD_ITALIC=>"-BoldOblique"
  })
}
