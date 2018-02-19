package util.fonts.parsers

import util.fonts.parsers.FontParser.FontMetric

import scala.collection.mutable


abstract class FontParser(val fontName: String) {

	val fontMetric: FontMetric = parseFont() //getFontMetrics()


	protected[this] def parseFont(): FontMetric

	def getCharWidth(char: Char): Float = {
		if (!fontMetric.fontMap.contains(char.toInt)) {
			0f
		} else {
			fontMetric.fontMap(char.toInt)
		}
	}

	def getCharHeight(char: Char): Float = {
		if (!fontMetric.fontMap.contains(char.toInt)) {
			0f
		} else {
			if (char.isLower) fontMetric.fontHeight._1 else fontMetric.fontHeight._2
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

	case class FontMetric(fontName: String, fontMap: Map[Int, Float], fontHeight: (Float, Float), fontDescriptor: Option[EmbeddedFontDescriptor])

}