package util.fonts.parsers

import util.fonts.parsers.FontParser.FontMetric

import scala.collection.mutable


abstract class FontParser(fontName: String) {

	lazy val fontMetric: FontMetric = getFontMetrics()

	protected[this] def getFontMetrics(): FontMetric = {
		if (!FontParser.fontsMetricMap.contains(fontName)) {
			FontParser.fontsMetricMap += fontName -> parseFont()
		}
		FontParser.fontsMetricMap(fontName)
	}

	protected[this] def parseFont(): FontMetric

	def getCharWidth(char: Char): Float = {
		if (!fontMetric.fontMap.contains(char.toInt)) {
			0f
		} else {
			fontMetric.fontMap(char.toInt)
		}
	}
}


object FontParser {

	case class GlyphWidth(firstChar: Short, lastChar: Short, widthList: List[Short])

	case class FontBBox(lowerLeftX: Short, lowerLeftY: Short, upperRightX: Short, upperRightY: Short) {
		override def toString: String = {
			lowerLeftX + " " + lowerLeftY + " " + upperRightX + " " + upperRightY
		}
	}

	case class EmbeddedFontDescriptor(ascent: Short, capHeight: Short, descent: Short, fontBBox: FontBBox
	                                  , italicAngle: Short, flags: Int, glyphWidth: GlyphWidth)

	case class FontMetric(fontName: String, fontMap: Map[Int, Float], fontDescriptor: Option[EmbeddedFontDescriptor])

	private[parsers] val fontsMetricMap = mutable.Map[String, FontMetric]()
}