package util.fonts.parsers


trait AbstractFontParser {


	case class FontAfmMetric(maxHeight: Int, fontMap: Map[Int, Float])

	def getCharWidth(char: Char, fontMetric: FontAfmMetric): Float
}
