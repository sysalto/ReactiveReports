package com.sysalto.render.util.wrapper

import com.sysalto.render.util.fonts.parsers.{FontParser, RFontParserFamily}
import com.sysalto.report.RFontAttribute
import com.sysalto.report.reportTypes.{RFont, ReportTxt}
import scalaz._
import Scalaz._

import scala.annotation.tailrec


class WordWrapN(fontFamilyMap: scala.collection.mutable.HashMap[String, RFontParserFamily]) {

	class RTextPos(val x: Float, val textLength: Float, val rtext: ReportTxt)

	class CharFN(val char: Char, val font: RFont)

	class WordN(val charList: List[CharFN])


	private[this] def getFontParser(font: RFont): FontParser = {
		val fontFamily = fontFamilyMap(font.fontName)
		font.attribute match {
			case RFontAttribute.NORMAL => fontFamily.regular
			case RFontAttribute.BOLD => fontFamily.bold.get
			case RFontAttribute.ITALIC => fontFamily.italic.get
			case RFontAttribute.BOLD_ITALIC => fontFamily.boldItalic.get
		}
	}

	private[this] def getCharWidth(char: CharFN): Float = {
		val font = char.font
		val fontParser: FontParser = getFontParser(font)
		fontParser.getCharWidth(char.char) * char.font.size
	}

	def getTextWidth(text: ReportTxt): Float = {
		val word = new WordN(text.txt.map(char => new CharFN(char, text.font)).toList)
		word.charList.foldLeft(0.toFloat)((total, char) => {
			total + getCharWidth(char)
		})
	}


	def getTextHeight(text: ReportTxt): Float = {
		val word = new WordN(text.txt.map(char => new CharFN(char, text.font)).toList)
		if (word.charList.isEmpty) {
			0
		} else {
			word.charList.map(char => getCharWidth(char)).max
		}
	}


	val calculateWordWidth: ReportTxt => Float = Memo.immutableHashMapMemo {
		s => {
			println("calculate:" + s)
			getTextWidth(s)
		}
	}



	private[this] def wrapWords(list: List[ReportTxt], max: Float): List[List[RTextPos]] = {
		@tailrec
		def helper(list: List[ReportTxt], max: Float, accum: List[List[RTextPos]]): List[List[RTextPos]] = {
			val l1 = list.zipWithIndex.map { case (item, index) =>
				if (index == 0) {
					item.copy(txt = item.txt.trim)
				} else item
			}
			val l2 = l1.map(item => new RTextPos(0, 0, item)).initz
			val idx1 = l2.indexWhere(list => list.map { case t => calculateWordWidth(t.rtext) }.sum > max)
			val idx = if (idx1 == -1) l2.length else idx1
			val line1 = l2(idx - 1)
			val line2 = line1.map(word => {
				val wordWidth = calculateWordWidth(word.rtext)
				new RTextPos(0, wordWidth, word.rtext)

			})
			if (idx1 == -1) {
				accum ::: List(line2)
			} else {
				helper(list.drop(line1.length), max, List(line2))
			}
		}

		helper(list, max, nil)
	}


	private[this]
	def wrapLine(line: ReportTxt, max: Float): List[List[RTextPos]] = {

		val txt = line.txt
		val lw = txt.split("\\s+").zipWithIndex.map { case (word, index) => (if (index == 0) "" else " ") + word }
		val l1 = lw.map(word => new ReportTxt(word, line.font)).toList
		val result = wrapWords(l1, max)
		result
	}

	private def calculateWordWrap(input: List[ReportTxt], max: Float): List[List[RTextPos]] = {

		input.flatMap(item=>wrapLine(item, max))


	}

	def wordWrap(input: List[ReportTxt], max: Float)
	            (implicit wordSeparators: List[Char]): List[List[RTextPos]] = {

		val l1=input.flatMap(line=>line.txt.split("\n").toList.map(item=>ReportTxt(item,line.font)))
		calculateWordWrap(l1, max)
	}

}


class RTextPosN(val x: Float, val textLength: Float, val rtext: ReportTxt)