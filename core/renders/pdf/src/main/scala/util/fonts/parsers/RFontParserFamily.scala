package util.fonts.parsers

import com.sysalto.report.reportTypes.RFontFamily

case class RFontParserFamily(fontName: String, fontFamily: RFontFamily, embedded: Boolean) {

	private[this] def getParser(name: String): FontParser = if (embedded) new AfmParser(name) else new TtfParser(name)

	var regular: FontParser = null
	var bold: Option[FontParser] = None
	var italic: Option[FontParser] = None
	var boldItalic: Option[FontParser] = None
	regular = getParser(fontFamily.regular)
	if (fontFamily.bold.isDefined) {
		bold = Some(getParser(fontFamily.bold.get))
	}
	if (fontFamily.italic.isDefined) {
		italic = Some(getParser(fontFamily.italic.get))
	}
	if (fontFamily.boldItalic.isDefined) {
		boldItalic = Some(getParser(fontFamily.boldItalic.get))
	}
}
