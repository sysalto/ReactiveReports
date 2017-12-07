package util.fonts.parsers


trait AbstractFontParser {

	case class FontMetric(fontMap: Map[Int, Float])

	protected[this] val fontMetric: FontMetric

	def getCharWidth(char: Char): Float = {
		if (!fontMetric.fontMap.contains(char.toInt)) {
			0f
		} else {
			fontMetric.fontMap(char.toInt)
		}
	}
}
