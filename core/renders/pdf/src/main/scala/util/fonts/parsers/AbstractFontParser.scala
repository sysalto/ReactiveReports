package util.fonts.parsers


trait AbstractFontParser {

	case class GlyphDef(glypMap: Map[String, Int])

	case class FontAfmMetric(maxHeight: Int, fontMap: Map[Int, Float])

	def parseFont(fontName: String): FontAfmMetric

	def getCharWidth(char: Char, fontMetric: FontAfmMetric): Float
}
