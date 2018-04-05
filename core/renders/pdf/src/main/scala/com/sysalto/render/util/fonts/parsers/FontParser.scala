package com.sysalto.render.util.fonts.parsers

import com.sysalto.render.util.fonts.parsers.FontParser.FontMetric

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

	class GlyphWidth(val firstChar: Short, val lastChar: Short, val widthList: List[Short])

	class FontBBox(val lowerLeftX: Short,val  lowerLeftY: Short,val  upperRightX: Short,val upperRightY: Short) {
		override def toString: String = {
			lowerLeftX + " " + lowerLeftY + " " + upperRightX + " " + upperRightY
		}
	}

	class EmbeddedFontDescriptor(val ascent: Short,val capHeight: Short,val descent: Short,val fontBBox: FontBBox
	                                  , val italicAngle: Short,val flags: Int,val glyphWidth: GlyphWidth)

	class FontMetric(val fontName: String,val fontMap: Map[Int, Float],val fontHeight: (Float, Float),val fontDescriptor: Option[EmbeddedFontDescriptor])

}