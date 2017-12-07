package util.fonts.parsers

import scala.collection.mutable


trait FontParser {

	case class FontMetric(fontMap: Map[Int, Float])

	protected[this] val fontMetric: FontMetric

	protected [this] val fontsMetricMap = mutable.Map[String, FontMetric]()

	def getCharWidth(char: Char): Float = {
		if (!fontMetric.fontMap.contains(char.toInt)) {
			0f
		} else {
			fontMetric.fontMap(char.toInt)
		}
	}
}
