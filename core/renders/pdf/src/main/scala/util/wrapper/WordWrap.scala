/*
 * ReactiveReports - Free Java /Scala Reporting Library.
 * Copyright (C) 2017 SysAlto Corporation. All rights reserved.
  *
 * Unless you have purchased a commercial license agreement from SysAlto
 * the following license terms apply:
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



package util.wrapper

import com.sysalto.report.RFontAttribute
import com.sysalto.report.reportTypes.{RFont, RText}
import util.fonts.parsers.AfmParser

import scala.annotation.tailrec
import scala.collection.mutable.ListBuffer

//TODO: see https://gist.github.com/adamretter/9d3f1b2c2c6b5e4cb574    modificat numele variabilelor

/**
	* Created by marian on 11/05/17.
	*/
object WordWrap {

	type Path[Key] = (Double, List[Key])

	@tailrec
	def calculate[Key](lookup: Map[Key, List[(Double, Key)]], fringe: List[Path[Key]], dest: Key,
	                   visited: Set[Key]): Path[Key] = fringe match {
		case (dist, path) :: rest => path match {
			case Nil => (0, List())
			case key :: _ =>
				if (key == dest) (dist, path.reverse)
				else {
					val paths = lookup(key).flatMap { case (d, key1) => if (!visited.contains(key1)) List((dist + d, key1 :: path)) else Nil }
					val sorted = (paths ++ rest).sortWith { case ((d1, _), (d2, _)) => d1 < d2 }
					calculate(lookup, sorted, dest, visited + key)
				}
		}
		case _ => (0, List())

	}

	//val fontAfmParser=new AfmParser("Helvetica")

	//implicit val glypList = fontAfmParser.parseGlyph()


	case class CharF(char: Char, font: RFont)

	case class Word(charList: List[CharF])

	@tailrec
	def stringToWord(ll: List[CharF], accum: ListBuffer[Word]): Unit = {
		if (!ll.exists(item => item.char == ' ')) {
			accum += Word(ll)
		} else {
			val ll1 = ll.splitAt(ll.indexWhere(item => item.char == ' ') + 1)
			accum += Word(ll1._1)
			stringToWord(ll1._2, accum)
		}
	}

	def getWordSize(word: Word): Float = {
		word.charList.foldLeft(0.toFloat)((total, char) => {
			val afmParser=new AfmParser((char.font.fontKeyName))
			val fontMetric = afmParser.fontAfmMetric
			total + afmParser.getCharWidth(char.char, fontMetric) * char.font.size
		})
	}

	def getCharSize(char: CharF): Float = {
		val afmParser=new AfmParser((char.font.fontKeyName))
		val fontMetric = afmParser.fontAfmMetric
		afmParser.getCharWidth(char.char, fontMetric) * char.font.size
	}

	def splitAtMax(item: Word, max: Float): (Word, Word) = {
		@tailrec
		def getMaxStr(word: Word): Word = {
			if (getWordSize(word) <= max) {
				word
			}
			else {
				getMaxStr(Word(word.charList.dropRight(1)))
			}
		}

		val maxStr = getMaxStr(item)
		(maxStr, Word(item.charList.drop(maxStr.charList.size)))
	}

	@tailrec
	def splitWord(word: Word, max: Float, accum: ListBuffer[Word]): Unit = {
		if (getWordSize(word) <= max) {
			accum += word
		} else {
			val (part1, part2) = splitAtMax(word, max)
			accum += part1
			splitWord(part2, max, accum)
		}
	}

	case class RTextPos(x: Float, textLength: Float, rtext: RText)

	@tailrec
	def wordToRTextPos(offset: Float, word: Word, accum: ListBuffer[RTextPos]): Unit = {
		if (word.charList.isEmpty) {
			return
		}
		if (word.charList.groupBy(char => char.font).size == 1) {
			// one font ->keep it together
			val str = word.charList.map(char => char.char)
			val rtext = RText(str.mkString, word.charList(0).font)
			accum += RTextPos(offset, getWordSize(word), rtext)
		} else {
			val firstFont = word.charList(0).font
			val i1 = word.charList.indexWhere(char => char.font != firstFont)
			val word1 = Word(word.charList.take(i1))
			accum += RTextPos(offset, getWordSize(word1), RText(word.charList.take(i1).map(char => char.char).mkString, firstFont))

			val word2 = Word(word.charList.drop(i1))
			wordToRTextPos(offset + getWordSize(word1), word2, accum)
		}
	}

	//  def combineRTextPos(input:List[RTextPos]):RTextPos={
	//    val str = input.map(item => item.rtext.txt).foldLeft("")((a,b)=>a+b)
	//    RTextPos(input(0).x, RText(str, input(0).rtext.font))
	//  }
	//
	//  @tailrec
	//  def mergeRTextPos(input: List[RTextPos], accum: ListBuffer[RTextPos]): Unit = {
	//    if (input.isEmpty) {
	//      return
	//    }
	//    val firstFont = input(0).rtext.font
	//    val i1 = input.indexWhere(item => item.rtext.font != firstFont)
	//    if (i1 == -1) {
	//      accum +=combineRTextPos(input)
	//    } else {
	//      val elem1=input.take(i1)
	//      val elem2=input.drop(i1)
	//      accum +=combineRTextPos(elem1)
	//      mergeRTextPos(elem2,accum)
	//    }
	//  }

	def lineToRTextPos(line: List[Word]): List[RTextPos] = {
		val result1 = ListBuffer[RTextPos]()
		var offset = 0f
		line.foreach(word => {
			wordToRTextPos(offset, word, result1)
			offset += getWordSize(word)
		})
		//    val result= ListBuffer[RTextPos]()
		//    mergeRTextPos(result1.toList,result)
		val result = result1
		if (!result.isEmpty && result.last.rtext.txt == " ") {
			result.dropRight(1).toList
		} else {
			result.toList
		}
	}

	private[this] def wordWrapInternal(input: List[RText], max: Float)(implicit wordSeparators: List[Char]): List[List[RTextPos]] = {

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
		val result1 = input.flatMap(item => item.txt.map(cc => CharF(cc, item.font)))
		val result = ListBuffer[Word]()
		stringToWord(result1, result)


		val wordList = result.flatMap(item => {
			val result = ListBuffer[Word]()
			splitWord(item, max, result)
			result.toList
		}).toList

		val list = wordList.map(item => getWordSize(item))


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

	@tailrec
	private[this] def wordWrapT(input: List[RText], max: Float, accum: ListBuffer[List[RTextPos]])(implicit wordSeparators: List[Char]): Unit = {
		val i1 = input.indexWhere(item => item.txt.contains("\n"))
		if (i1 == -1) {
			accum ++= wordWrapInternal(input, max)
		} else {
			val l1 = input.take(i1)
			val elem = input(i1)
			val i2 = elem.txt.indexOf('\n')
			val list1 = input.take(i1) ++ List(RText(elem.txt.substring(0, i2), elem.font))
			val list2 = List(RText(elem.txt.substring(i2 + 1), elem.font)) ++ input.drop(i1 + 1)
			accum ++= wordWrapInternal(list1, max)
			wordWrapT(list2, max, accum)
		}
	}


	def wordWrap(input: List[RText], max: Float)(implicit wordSeparators: List[Char]): List[List[RTextPos]] = {
		val result = ListBuffer[List[RTextPos]]()
		wordWrapT(input, max, result)
		result.toList
	}


	def main(x: Array[String]): Unit = {
		val list = List(RText("ii ii ii", RFont(8)), RText("11 11 \n11 abc II jj kkkk ", RFont(8)), RText(" WWWABWCD rrr", RFont(12)), RText("iii ", RFont(10)),
			RText("uuu", RFont(10, attribute = RFontAttribute.BOLD)))
		implicit val wordSeparators = List(',', '.')
		//    val lines = wordWrapInternal(list, 50)
		//    println(lines.mkString("\n"))
		val result = wordWrap(list, 50)
		println(result.mkString("\n"))
	}
}