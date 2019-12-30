package com.sysalto.report.reportTypes

case class RFontFamily(name: String, regular: String, bold: Option[String] = None, italic: Option[String] = None,
                       boldItalic: Option[String] = None) {
}
