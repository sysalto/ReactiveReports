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
			val l2 = list.map(item => new RTextPos(0, 0, item)).initz
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
	def wrapLineOLd(line: ReportTxt, max: Float): List[List[RTextPos]] = {

		val txt = line.txt
		val lw = txt.split("\\s+").zipWithIndex.map { case (word, index) => (if (index == 0) "" else " ") + word }
		val l1 = lw.map(word => new ReportTxt(word, line.font)).toList
		val result = wrapWords(l1, max)
		result
	}


	private[this]
	def wrapLine(line: List[ReportTxt], max: Float): List[List[RTextPos]] = {

		val l1 = line.flatMap(line => {
			val txt = line.txt

			val l1 = txt.toList
			val l2 = l1.groupWhen((c1, c2) => {
				if (c1 != ' ' && c2 != ' ') {
					true
				} else {
					c1 != ' ' && c2 == ' '
				}
			})
			val lw = l2.map(l => l.toList.mkString)

			lw.map(word => new ReportTxt(word, line.font)).toList
		})
		val result = wrapWords(l1, max)
		result
	}

	def wordWrap(input: List[ReportTxt], max: Float)
	            (implicit wordSeparators: List[Char]): List[List[RTextPos]] = {

		class ReportTxtNl(val reportTxt: ReportTxt, val nl: Boolean = false)

		val l1 = input.flatMap(item => {
			val txt = item.txt
			if (txt.contains("\n")) {
				val r1 = txt.split("\n")
				r1.zipWithIndex.map { case (l, index) => new ReportTxtNl(ReportTxt(l, item.font), index > 0) }
			} else {
				List(new ReportTxtNl(item))
			}
		}).filter(item => !item.reportTxt.txt.isEmpty)

		val l2 = l1.groupWhen((item1, item2) => !item1.nl && !item2.nl)
		l2.flatMap(line => {
			val lineList = line.map(item => item.reportTxt)
			wrapLine(lineList.toList, max)
		})

	}

}


class RTextPosN(val x: Float, val textLength: Float, val rtext: ReportTxt)