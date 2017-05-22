package util.wrapper

import com.sysalto.report.reportTypes.{RFont, RText}
import util.FontAfmParser
import util.FontAfmParser.{FontAfmMetric, parseFont, parseGlyph}

import scala.annotation.tailrec
import scala.collection.mutable.ListBuffer

/**
  * Created by marian on 11/05/17.
  */
object WordWrap {

  type Path[Key] = (Double, List[Key])

  @tailrec
  def calculate[Key](lookup: Map[Key, List[(Double, Key)]], fringe: List[Path[Key]], dest: Key,
                     visited: Set[Key]): Path[Key] = fringe match {
    case (dist, path) :: fringe_rest => path match {
      case Nil => (0, List())
      case key :: path_rest =>
        if (key == dest) (dist, path.reverse)
        else {
          val paths = lookup(key).flatMap { case (d, key) => if (!visited.contains(key)) List((dist + d, key :: path)) else Nil }
          val sorted_fringe = (paths ++ fringe_rest).sortWith { case ((d1, _), (d2, _)) => d1 < d2 }
          calculate(lookup, sorted_fringe, dest, visited + key)
        }
    }

  }

  def test0(): Unit = {
    val lookup = Map(
      "a" -> List((7.0, "b"), (9.0, "c"), (14.0, "f")),
      "b" -> List((10.0, "c"), (15.0, "d")),
      "c" -> List((11.0, "d"), (2.0, "f")),
      "d" -> List((6.0, "e")),
      "e" -> List((9.0, "f")),
      "f" -> Nil
    )
    val res = calculate[String](lookup, List((0, List("a"))), "e", Set())
    println(res)
  }


  implicit val glypList = parseGlyph()


  def getSpaceSize(rtext: RText): Float = getWordSizeOld(RText(" ", rtext.font))

  def getWordSizeOld(word: RText): Float = {
    val fontMetric = parseFont(word.font.fontKeyName)
    FontAfmParser.getStringWidth(word.txt, fontMetric) * word.font.size
  }

  def splitAtMaxOld(item: RText, max: Float): (RText, RText) = {
    @tailrec
    def getMaxStr(rtext: RText): RText = {
      if (getWordSizeOld(rtext) <= max) {
        rtext
      }
      else {
        getMaxStr(RText(rtext.txt.substring(rtext.txt.length - 1), rtext.font))
      }
    }

    val maxStr = getMaxStr(item)
    (maxStr, RText(item.txt.substring(maxStr.txt.size)))
  }

  @tailrec
  def splitWordOld(word: RText, max: Float, accum: ListBuffer[RText]): Unit = {
    if (getWordSizeOld(word) <= max) {
      accum += word
    } else {
      val (part1, part2) = splitAtMaxOld(word, max)
      accum += part1
      splitWordOld(part2, max, accum)
    }
  }

  case class CharF(char: Char, font: RFont)

  case class Word(charList: List[CharF])

  @tailrec
  def stringToWord(ll: List[CharF], accum: ListBuffer[Word]): Unit = {
    if (!ll.exists(item => item.char == ' ')) {
      accum += Word(ll)
    } else {
      val ll1 = ll.splitAt(ll.indexWhere(item => item.char == ' '))
      accum += Word(ll1._1)
      stringToWord(ll1._2.tail, accum)
    }
  }

  def getWordSize(word: Word): Float = {
    word.charList.foldLeft(0.toFloat)((total,char)=>{
      val fontMetric = parseFont(char.font.fontKeyName)
      total+FontAfmParser.getCharWidth(char.char,fontMetric)*char.font.size
    })
  }

  def splitAtMax(item: Word, max: Float): (Word, Word) = {
    @tailrec
    def getMaxStr(word: Word): Word = {
      if (getWordSize(word) <= max) {
        word
      }
      else {
        getMaxStr(Word(word.charList.tail))
      }
    }

    val maxStr = getMaxStr(item)
    (maxStr,Word(item.charList.drop(maxStr.charList.size)))
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

  def wordWrap(input: List[RText], max: Float)(implicit wordSeparators: List[Char]): List[List[RText]] = {

    val a1 = input.flatMap(item => item.txt.map(cc => CharF(cc, item.font)))




    val result = ListBuffer[Word]()
    stringToWord(a1, result)


    val wordList = result.flatMap(item => {
      val result = ListBuffer[Word]()
      splitWord(item, max, result)
      result.toList
    })

    println(wordList.mkString("\n"))

    //----
    val i1 = input.map(item => RText(item.txt.foldLeft("")((sum, b) => sum + (if (wordSeparators.contains(b)) b + " " else b)), item.font))
    val i2 = i1.flatMap(item => {
      item.txt.replaceAll("\\s+", " ").split(" ").toList.map(str => RText(str, item.font))
    })
    val wordListOld = i2.flatMap(item => {
      val result = ListBuffer[RText]()
      splitWordOld(item, max, result)
      result.toList
    })
    val list = wordListOld.map(item => getWordSizeOld(item))

    // function that calculate the size of a string including spaces
    def size(l: List[Float], rtext: RText): Float = {
      l.sum + getSpaceSize(rtext) * (l.size - 1)
    }


    def calc(i1: Int, i2: Int, rtext: RText): Option[Float] = {
      val l1 = list.slice(i1, i2 + 1)
      val result = size(l1, rtext)
      if (result <= max) {
        val dif = max - result
        Some(dif * dif)
      } else {
        None
      }
    }

    val l1 = list.indices
    val l2 = l1.combinations(2).map(item => (item.head, item.tail.head)) ++ l1.map(item => (item, item))
    val mapCost = l2.map(item => item -> calc(item._1, item._2, input(0))).filter { case (key, value) => value.isDefined }.
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
      wordListOld.slice(indiceList(i), indiceList(i + 1))
    }
    lines.toList
  }


  def main(x: Array[String]): Unit = {
    val list = List(RText("AAA III", RFont(8)), RText(" WWWABCD rrr", RFont(12)), RText("iii ", RFont(10)), RText("uuu", RFont(10)))
    implicit val wordSeparators = List(',', '.')
    val lines = wordWrap(list, 50)
    println(lines.mkString("\n"))
  }
}