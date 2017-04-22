package com.sysalto.report.reportTypes

import com.sysalto.report.RFontAttribute

/**
  * Created by marian on 3/4/17.
  */
case class RFont(var size: Int, var fontName: String = "Helvetica", var attribute: RFontAttribute.Value = RFontAttribute.NORMAL, var color: RColor = RColor(0, 0, 0)) {
  def fontKeyName = fontName +
    (attribute match {
    case RFontAttribute.NORMAL => ""
    case RFontAttribute.BOLD=>"-Bold"
    case RFontAttribute.ITALIC=>"-Oblique"
    case RFontAttribute.BOLD_ITALIC=>"-BoldOblique"
  })
}
