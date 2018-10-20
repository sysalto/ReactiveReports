/*
 * ReactiveReports - Free Java /Scala Reporting Library.
 * Copyright (C) 2017 SysAlto Corporation. All rights reserved.
 *
 * This program is part of ReactiveReports.
 *
 * ReactiveReports is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ReactiveReports is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY. Without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with ReactiveReports.
 * If not, see https://www.gnu.org/licenses/lgpl-3.0.en.html.
 */


package com.sysalto.render.util.wrapper

import com.sysalto.report.RFontAttribute
import com.sysalto.report.reportTypes.{RFont, ReportTxt}
import com.sysalto.render.util.fonts.parsers.{AfmParser, FontParser, RFontParserFamily, TtfParser}

import scala.annotation.tailrec
import scala.collection.mutable.ListBuffer


class WordWrap(fontFamilyMap: scala.collection.mutable.HashMap[String, RFontParserFamily]) {

	private[this] type Cost[T] = (Double, List[T])

	@tailrec
	private[this] def calculate[T](widthList: Map[T, List[(Double, T)]], accumList: List[Cost[T]], dest: T,
	                               visited: Set[T]): Cost[T] = accumList match {
		case (dist, path) :: rest => path match {
			case Nil => (0, List())
			case key :: _ =>
				if (key == dest) (dist, path.reverse)
				else {
					val paths = widthList(key).flatMap { case (d, key1) => if (!visited.contains(key1)) List((dist + d, key1 :: path)) else Nil }
					val sorted = (paths ++ rest).sortWith { case ((d1, _), (d2, _)) => d1 < d2 }
					calculate(widthList, sorted, dest, visited + key)
				}
		}
		case Nil => (0, List())

	}

	class CharF(val char: Char, val font: RFont)

	class Word(val charList: List[CharF])

	@tailrec
	private[this] def stringToWord(ll: List[CharF], accum: ListBuffer[Word]): Unit = {
		if (!ll.exists(item => item.char == ' ')) {
			accum += new Word(ll)
		} else {
			val l1 = ll.dropWhile(item => item.char == ' ')
			val ll1 = l1.splitAt(l1.indexWhere(item => item.char == ' ') + 1)
			val newWordList = ll1._1
			val newWord = newWordList.take(newWordList.length - 1)
			accum += new Word(newWord)
			stringToWord(ll1._2, accum)
		}
	}

	private[this] object FontType extends Enumeration {
		val Afm, Ttf = Value
	}

	private[this] def getFontParser(font: RFont): FontParser = {
		val fontFamily = fontFamilyMap(font.fontName)
		font.attribute match {
			case RFontAttribute.NORMAL => fontFamily.regular
			case RFontAttribute.BOLD => fontFamily.bold.get
			case RFontAttribute.ITALIC => fontFamily.italic.get
			case RFontAttribute.BOLD_ITALIC => fontFamily.boldItalic.get
		}
	}

	private[this] def getWordSizeIncludingSpace(word: Word): Float = {
		if (word.charList.isEmpty) {
			0f
		} else {
			val space = new CharF(' ', word.charList.head.font)
			(space :: word.charList).foldLeft(0.toFloat)((total, char) => {
				total + getCharWidth(char)
			})
		}
	}

	def getTextWidth(text: ReportTxt): Float = {
		val word = new Word(text.txt.map(char => new CharF(char, text.font)).toList)
		word.charList.foldLeft(0.toFloat)((total, char) => {
			total + getCharWidth(char)
		})
	}


	def getTextHeight(text: ReportTxt): Float = {
		val word = new Word(text.txt.map(char => new CharF(char, text.font)).toList)
		if (word.charList.isEmpty) {
			0
		} else {
			word.charList.map(char => getCharWidth(char)).max
		}
	}


	private[this] def getCharWidth(char: CharF): Float = {
		val font = char.font
		val fontParser: FontParser = getFontParser(font)
		fontParser.getCharWidth(char.char) * char.font.size
	}

	private[this] def getCharHeight(char: CharF): Float = {
		val font = char.font
		val fontParser: FontParser = getFontParser(font)
		fontParser.getCharHeight(char.char) * char.font.size
	}


	private[this] def splitAtMax(item: Word, max: Float): (Word, Word) = {
		@tailrec
		def getMaxStr(word: Word): Word = {
			if (getWordSizeIncludingSpace(word) <= max) {
				word
			}
			else {
				getMaxStr(new Word(word.charList.dropRight(1)))
			}
		}

		val maxStr = getMaxStr(item)
		(maxStr, new Word(item.charList.drop(maxStr.charList.size)))
	}

	@tailrec
	private[this] def splitWord(word: Word, max: Float, accum: ListBuffer[Word]): Unit = {
		if (getWordSizeIncludingSpace(word) <= max) {
			accum += word
		} else {
			val (part1, part2) = splitAtMax(word, max)
			accum += part1
			splitWord(part2, max, accum)
		}
	}


	@tailrec
	private[this] def wordToRTextPos(offset: Float, word: Word, accum: ListBuffer[RTextPos]): Unit = {
		if (word.charList.isEmpty) {
			return
		}
		if (word.charList.groupBy(char => char.font).size == 1) {
			// one font ->keep it together
			val str = word.charList.map(char => char.char)
			val rtext = ReportTxt(str.mkString, word.charList(0).font)
			accum += new RTextPos(offset, getWordSizeIncludingSpace(word), rtext)
		} else {
			val firstFont = word.charList(0).font
			val i1 = word.charList.indexWhere(char => char.font != firstFont)
			val word1 = new Word(word.charList.take(i1))
			accum += new RTextPos(offset, getWordSizeIncludingSpace(word1), ReportTxt(word.charList.take(i1).map(char => char.char).mkString, firstFont))

			val word2 = new Word(word.charList.drop(i1))
			wordToRTextPos(offset + getWordSizeIncludingSpace(word1), word2, accum)
		}
	}

	private[this] def lineToRTextPos(line: List[Word]): List[RTextPos] = {
		val result1 = ListBuffer[RTextPos]()
		var offset = 0f
		line.foreach(word => {
			wordToRTextPos(offset, word, result1)
			offset += getWordSizeIncludingSpace(word)
		})
		//    val result= ListBuffer[RTextPos]()
		//    mergeRTextPos(result1.toList,result)

		val last = result1.last

		val lastCharList = line.reverse.find(item => item.charList.nonEmpty)

		if (lastCharList.isDefined) {
			val space = new CharF(' ', lastCharList.get.charList.head.font)
			val length = result1.length
			result1.remove(length - 1)
			result1 += new RTextPos(last.x, last.textLength - getCharWidth(space), last.rtext)
		}

		val result = result1
		if (!result.isEmpty && result.last.rtext.txt == " ") {
			result.dropRight(1).toList
		} else {
			result.toList
		}
	}

	private[this] def wordWrapInternal(input: List[ReportTxt], max: Float)(implicit wordSeparators: List[Char]): List[List[RTextPos]] = {

		// function that calculate the size of a string including spaces
		def size(l: List[Float], font: RFont): Float = {
			l.sum
		}


		def calc(list: List[Float], i1: Int, i2: Int, font: RFont): Option[Float] = {
			val l1 = list.slice(i1, i2 + 1).toList
			val result = size(l1, font)
			if (result <= max) {
				val dif = max - result
				Some(dif * dif)
			} else {
				None
			}
		}

		val result1 = input.flatMap(item => item.txt.map(cc => new CharF(cc, item.font)))
		val result = ListBuffer[Word]()
		stringToWord(result1, result)


		val wordList = result.flatMap(item => {
			val result = ListBuffer[Word]()
			splitWord(item, max, result)
			result.toList
		}).toList

		val list = wordList.map(item => getWordSizeIncludingSpace(item))


		val l1 = list.indices
		val l2 = l1.combinations(2).map(item => (item.head, item.tail.head)) ++ l1.map(item => (item, item))
		val mapCost = l2.map(item => item -> calc(list, item._1, item._2, input(0).font)).filter { case (key, value) => value.isDefined }.
			map { case (key, value) => key -> value.get }.toMap

		val rr = for {i <- list.length - 1 to 0 by -1
		              j <- list.length to i by -1
		              key = (i, j - 1) if mapCost.contains(key)
		              cost = mapCost(key)
		}
			yield (i, j, cost)

		val rr1 = rr.map { case (a, b, c) => a }.toSet.toList
		val rr2 = rr1.map(item => {
			item -> rr.filter { case (a, b, c) => a == item }.map { case (a, b, c) => (c.toDouble, b) }.toList
		}).toMap

		val res = calculate[Int](rr2, List((0, List(0))), list.length, Set())


		val indiceList = res._2

		val lines = for (i <- 0 to indiceList.size - 2) yield {
			wordList.slice(indiceList(i), indiceList(i + 1))
		}
		lines.map(line => lineToRTextPos(line)).toList
	}

	private[this] def wordWrapInternalSimple(input: List[ReportTxt], max: Float)(implicit wordSeparators: List[Char]): List[List[RTextPos]] = {
		val result1 = input.flatMap(item => item.txt.map(cc => new CharF(cc, item.font)))
		val result = ListBuffer[Word]()
		stringToWord(result1, result)


		val wordList = result.flatMap(item => {
			val result = ListBuffer[Word]()
			splitWord(item, max, result)
			result.toList
		}).toList

		val wrapResult = ListBuffer[List[RTextPos]]()
		var wrapRow = ListBuffer[Word]()
		val list = wordList.map(item => (item, getWordSizeIncludingSpace(item)))
		var length = 0f
		list.foreach {
			case (word, size) => {
				if ((length + size) <= max) {
					wrapRow += word
					length += size
				} else {
					wrapResult += lineToRTextPos(wrapRow.toList)
					wrapRow.clear()
					length = 0
				}
			}
		}
		if (length > 0) {
			wrapResult += lineToRTextPos(wrapRow.toList)
		}
		wrapResult.toList
	}

	@tailrec
	private[this] def wordWrapT(input: List[ReportTxt], max: Float, accum: ListBuffer[List[RTextPos]])(implicit wordSeparators: List[Char]): Unit = {
		val i1 = input.indexWhere(item => item.txt.contains("\n"))
		if (i1 == -1) {
			//			accum ++= wordWrapInternal(input, max)
			accum ++= wordWrapInternalSimple(input, max)
		} else {
			val l1 = input.take(i1)
			val elem = input(i1)
			val i2 = elem.txt.indexOf('\n')
			val list1 = input.take(i1) ++ List(ReportTxt(elem.txt.substring(0, i2), elem.font))
			val list2 = List(ReportTxt(elem.txt.substring(i2 + 1), elem.font)) ++ input.drop(i1 + 1)
			//			accum ++= wordWrapInternal(list1, max)
			accum ++= wordWrapInternalSimple(list1, max)
			wordWrapT(list2, max, accum)
		}
	}


	private def calculateWordWrap(input: List[ReportTxt], max: Float): List[List[RTextPos]] = {
		import scalaz._
		import Scalaz._

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
			def wrapWords(list:List[ReportTxt]):List[List[RTextPos]]={
				val l2 = list.map(item => new RTextPos(0, 0, item)).initz
				val idx1 = l2.indexWhere(list => list.map { case t => calculate2(t) }.sum > max)
				val idx = if (idx1 == -1) l2.length else idx1
				val line1 = l2(idx - 1)
				val line2 = line1.map(word => {
					val wordWidth = calculate3(word)
					new RTextPos(0, wordWidth, word.rtext)

				})
				List(line2)++ (if (idx1== -1 ) List() else {
					wrapWords(list.drop(line1.length))
				})
			}

			val txt = line.txt
			val lw = txt.split("\\s+").zipWithIndex.map { case (word, index) => (if (index == 0) "" else " ") + word }
			val l1 = lw.map(word => new ReportTxt(word, line.font)).toList
			val result=wrapWords(l1)
			result
		}

		wrapLine(input.head)

	}


	def wordWrap(input: List[ReportTxt], max: Float)
	            (implicit wordSeparators: List[Char]): List[List[RTextPos]] = {

		if (true) {
			return calculateWordWrap(input, max)
		}

		val result = ListBuffer[List[RTextPos]]()
		wordWrapT(input, max, result)
		result.toList
	}


}


class RTextPos(val x: Float, val textLength: Float, val rtext: ReportTxt)