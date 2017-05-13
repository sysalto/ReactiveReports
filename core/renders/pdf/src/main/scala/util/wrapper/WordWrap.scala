package util.wrapper

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


  def getWordSize(word: String)(implicit fontMetric: FontAfmMetric): Float = FontAfmParser.getStringWidth(word, fontMetric)

  def getSpaceSize()(implicit fontMetric: FontAfmMetric): Float = getWordSize(" ")

  def splitAtMax(item: String, max: Float)(implicit fontMetric: FontAfmMetric): (String, String) = {
    @tailrec
    def getMaxStr(str: String): String = {
      if (getWordSize(str) <= max) {
        str
      }
      else {
        getMaxStr(str.substring(str.length - 1))
      }
    }
    val maxStr=getMaxStr(item)
    (maxStr, item.substring(maxStr.size))
  }

  @tailrec
  def splitWord(word: String, max: Float, accum: ListBuffer[String])(implicit fontMetric: FontAfmMetric): Unit = {
    if (getWordSize(word) <= max) {
      accum += word
    } else {
      val (part1, part2) = splitAtMax(word, max)
      accum += part1
      splitWord(part2, max, accum)
    }
  }


  def wordWrap(input: String, max: Float)(implicit wordSeparators: List[Char], fontMetric: FontAfmMetric): List[List[String]] = {
    val input0 = input.foldLeft("")((sum, b) => sum + (if (wordSeparators.contains(b)) b + " " else b)).trim
    val input1 = input0.replaceAll("\\s+", " ").split(" ").toList
    val wordList = input1.flatMap(item => {
      val result = ListBuffer[String]()
      splitWord(item, max, result)
      result.toList
    })
    val list = wordList.map(item => getWordSize(item))

    def size(l: List[Float]): Float = {
      l.sum + getSpaceSize * (l.size - 1)
    }

    def calc(i1: Int, i2: Int): Option[Float] = {
      val l1 = list.slice(i1, i2 + 1)
      val result = size(l1)
      if (result <= max) {
        val dif = max - result
        Some(dif * dif)
      } else {
        None
      }
    }

    val l1 = list.indices
    val l2 = l1.combinations(2).map(item => (item.head, item.tail.head)) ++ l1.map(item => (item, item))
    val mapCost = l2.map(item => item -> calc(item._1, item._2)).filter { case (key, value) => value.isDefined }.
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
    lines.toList
  }

  def main(x: Array[String]): Unit = {
    implicit val fontMetric = parseFont("Helvetica")
    implicit val wordSeparators = List(',', '.')
    val s = "Catelus cu parul cret fura rata din cotet.El se jura ca nu fura,dar l-am prins cu rata-n gura."
    val lines = wordWrap(s, 10)
    println(lines.map(item => item.mkString(" ")).mkString("\n"))
  }
}