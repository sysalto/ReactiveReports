package util.fonts.parsers

import util.fonts.parsers.FontParser.FontMetric

import scala.collection.mutable


trait FontParser {

	protected[this] val fontMetric: FontMetric



	def getCharWidth(char: Char): Float = {
		if (!fontMetric.fontMap.contains(char.toInt)) {
			0f
		} else {
			fontMetric.fontMap(char.toInt)
		}
	}
}


object FontParser {

	case class FontMetric(fontMap: Map[Int, Float])

	private[parsers] val fontsMetricMap = mutable.Map[String, FontMetric]()
}