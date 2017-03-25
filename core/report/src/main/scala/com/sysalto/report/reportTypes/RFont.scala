package com.sysalto.report.reportTypes

import com.sysalto.report.RFontAttribute

/**
  * Created by marian on 3/4/17.
  */
case class RFont(var size: Int, var fontName: Option[String] = None, var attribute: RFontAttribute.Value = RFontAttribute.NORMAL, var color: RColor = RColor(0, 0, 0))
