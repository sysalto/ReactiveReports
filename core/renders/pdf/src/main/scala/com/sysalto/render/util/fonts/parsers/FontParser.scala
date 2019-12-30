package com.sysalto.render.util.fonts.parsers

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.sysalto.render.util.fonts.parsers.FontParser.FontMetric

import scala.collection.mutable

@JsonTypeInfo(use = Id.CLASS, property = "className")
abstract class FontParser(val fontName: String)(protected val className:String) {
	def getFontMetric(): FontMetric

	var charList=List[Int]()


	def getCharWidth(char: Char): Float = {
		if (!getFontMetric.fontWidth.contains(char.toInt)) {
			0f
		} else {
			getFontMetric.fontWidth(char.toInt)
		}
	}

	def getCharHeight(char: Char): Float = {
		if (!getFontMetric.fontWidth.contains(char.toInt)) {
			0f
		} else {
			if (char.isLower) getFontMetric.fontHeight._1 else getFontMetric.fontHeight._2
		}
	}

}


object FontParser {

	class GlyphWidth(val firstChar: Short, val lastChar: Short, val widthList: List[Short])

	class FontBBox(val lowerLeftX: Short,val  lowerLeftY: Short,val  upperRightX: Short,val upperRightY: Short) {
		override def toString: String = s"$lowerLeftX $lowerLeftY $upperRightX $upperRightY"
	}

	class EmbeddedFontDescriptor(val ascent: Short,val capHeight: Short,val descent: Short,val fontBBox: FontBBox
	                                  , val italicAngle: Short,val flags: Int,val glyphWidth: GlyphWidth,val isOtf:Boolean,val panose:String)

	class FontMetric(val fontName: String, val fontWidth: Map[Int, Float],val fontGlyphNbr: Map[Integer, Int], val fontHeight: (Float, Float), val fontDescriptor: Option[EmbeddedFontDescriptor])

}