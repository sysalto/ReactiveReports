package com.sysalto.render.util.wrapper

import com.sysalto.render.util.fonts.parsers.{FontParser, RFontParserFamily}
import com.sysalto.report.RFontAttribute
import com.sysalto.report.reportTypes.{RFont, ReportTxt}
import scalaz._
import Scalaz._


class WordWrapN(fontFamilyMap: scala.collection.mutable.HashMap[String, RFontParserFamily]) {
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


	private def calculateWordWrap(input: List[ReportTxt], max: Float): List[List[RTextPos]] = {












		val calculate3: RTextPos => Float = Memo.immutableHashMapMemo {
			s => {
				println("calculate3:" + s)
				getTextWidth(s.rtext)
			}
		}

		val calculate2: RTextPos => Float = Memo.immutableHashMapMemo {
			token => {
				val result = calculate3(token)
				println("calculate2:" + token + " result:" + result)
				result
			}
		}

		def wrapLine(line: ReportTxt): List[List[RTextPos]] = {

			//			@tailrec
			def wrapWords(list: List[ReportTxt]): List[List[RTextPos]] = {
				val l2 = list.map(item => new RTextPos(0, 0, item)).initz
				val idx1 = l2.indexWhere(list => list.map { case t => calculate2(t) }.sum > max)
				val idx = if (idx1 == -1) l2.length else idx1
				val line1 = l2(idx - 1)
				val line2 = line1.map(word => {
					val wordWidth = calculate3(word)
					new RTextPos(0, wordWidth, word.rtext)

				})
				List(line2) ++ (if (idx1 == -1) List() else {
					wrapWords(list.drop(line1.length))
				})
			}

			val txt = line.txt
			val lw = txt.split("\\s+").zipWithIndex.map { case (word, index) => (if (index == 0) "" else " ") + word }
			val l1 = lw.map(word => new ReportTxt(word, line.font)).toList
			val result = wrapWords(l1)
			result
		}

		wrapLine(input.head)

	}

	def wordWrap(input: List[ReportTxt], max: Float)
	            (implicit wordSeparators: List[Char]): List[List[RTextPos]] = {

		calculateWordWrap(input, max)
	}

}


class RTextPosN(val x: Float, val textLength: Float, val rtext: ReportTxt)