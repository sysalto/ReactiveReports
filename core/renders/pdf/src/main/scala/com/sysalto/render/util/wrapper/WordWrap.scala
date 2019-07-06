package com.sysalto.render.util.wrapper

import com.sysalto.render.util.fonts.parsers.{FontParser, RFontParserFamily}
import com.sysalto.report.RFontAttribute
import com.sysalto.report.reportTypes.{RFont, ReportTxt}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer


class WordWrap(fontFamilyMap: scala.collection.mutable.HashMap[String, RFontParserFamily])
              (implicit wordSeparators: List[Char]) {

  class RTextPos(val x: Float, val textLength: Float, val rtext: ReportTxt)

  class CharFN(val char: Char, val font: RFont)

  private[this] def getFontParser(font: RFont): FontParser = {
    val fontFamily = fontFamilyMap(font.fontName)
    font.attribute match {
      case RFontAttribute.NORMAL => fontFamily.regular
      case RFontAttribute.BOLD => fontFamily.bold.get
      case RFontAttribute.ITALIC => fontFamily.italic.get
      case RFontAttribute.BOLD_ITALIC => fontFamily.boldItalic.get
    }
  }

  private[this] def splitNewLines(input: List[ReportTxt]): Seq[Seq[ReportTxt]] = {
    val r1 = ListBuffer[List[ReportTxt]]()
    val crtLine = ListBuffer[ReportTxt]()
    input.foreach(reportTxt => {
      if (reportTxt.txt.contains("\n")) {
        val l1 = reportTxt.txt.split("\n").toList.map(txt => ReportTxt(txt, reportTxt.font))
        val lastItem = l1.last
        l1.foreach(item => {
          crtLine += item
          if (item != lastItem) {
            r1 += crtLine.toList
            crtLine.clear()
          }
        })
      } else {
        crtLine += reportTxt
      }
    })
    r1 += crtLine.toList
    r1.toSeq
  }

  private[this] def getCharWidth(char: CharFN): Float = {
    val map=new mutable.HashMap[CharFN,Float]()

    def calcWidth(char: CharFN) = {
      val font = char.font
      val fontParser: FontParser = getFontParser(font)
      fontParser.getCharWidth(char.char) * char.font.size
    }
    val result=map.get(char)
    if (result.isDefined) {
      result.get
    } else {
      val width= calcWidth(char)
      map.put(char,width)
      width
    }
  }


  private[this] def split(input: Seq[CharFN], max: Float): Seq[Seq[CharFN]] = {
    val result = ListBuffer[Seq[CharFN]]()
    val resultCrtLine = ListBuffer[CharFN]()
    var workingInput = ListBuffer[CharFN]()
    workingInput ++= input

    while (workingInput.nonEmpty) {
      val list = workingInput.map(charFN => (charFN, getCharWidth(charFN)))
      val idx1 = list.zipWithIndex.indexWhere {
        case ((charFN, width), index) => {
          list.take(index + 1).map(item => item._2).sum > max
        }
      }
      if (idx1 == -1) {
        // all items fit
        result += workingInput.clone().toSeq
        workingInput.clear()
      } else {
        val idx2 = workingInput.take(idx1 - 1).lastIndexWhere(charFN => wordSeparators.contains(charFN.char))
        if (idx2 >= 0) {
          // found
          resultCrtLine ++= workingInput.take(idx2 + 1)
          result += resultCrtLine.clone().toSeq
          resultCrtLine.clear()
          workingInput = workingInput.drop(idx2 + 1)
        } else {
          // cut the current word
          resultCrtLine ++= workingInput.take(idx1 - 1)
          result += resultCrtLine.clone.toSeq
          resultCrtLine.clear()
          workingInput = workingInput.drop(idx1 - 1)
        }
      }
    }
    result.toSeq
  }

  private[this] def convertToTextPos(input: Seq[CharFN]): Seq[RTextPos] = {
    val result = ListBuffer[RTextPos]()
    var position = 0f
    var textLength = 0f
    var currentFont: RFont = null
    var currentText = new StringBuilder()
    input.foreach(charFN => {
      if (currentFont == null || charFN.font == currentFont) {
        if (currentFont == null) {
          currentFont = charFN.font
        }
        currentText.append(charFN.char)
        textLength += getCharWidth(charFN)
      }
      if (charFN.font != currentFont) {
        result += new RTextPos(position, textLength, ReportTxt(currentText.toString(), currentFont))
        position += textLength
        currentText.clear()
        currentFont = charFN.font
        currentText.append(charFN.char)
        textLength += getCharWidth(charFN)
      }
    })
    result += new RTextPos(position, textLength, ReportTxt(currentText.toString(), currentFont))
    result.toSeq
  }

  private[this] def wrapLine(line: Seq[ReportTxt], max: Float): Seq[Seq[RTextPos]] = {
    val l1 = split(line.flatMap(item => item.txt.map(char => new CharFN(char, item.font))), max)
    l1.map(line => convertToTextPos(line))
  }


  def getTextWidth(text: ReportTxt): Float = {
    text.txt.map(char => getCharWidth(new CharFN(char, text.font))).sum
  }

  def wordWrap(input: List[ReportTxt], max: Float): List[List[RTextPos]] = {
    val l1 = splitNewLines(input)

    val result = ListBuffer[Seq[RTextPos]]()
    val resultCrtLine = ListBuffer[Seq[RTextPos]]()
    l1.foreach(line => {
      result ++= wrapLine(line, max)
    })
    result.map(item => item.toList).toList
  }
}



