package com.sysalto.report.reportTypes

import com.sysalto.report.RFontAttribute

import scala.collection.mutable.ListBuffer

/**
  * Created by marian on 3/4/17.
  */
case class RText(txt: String, var font: RFont = RFont(10)) {

  def this(txt: String) = {
    this(txt, RFont(10))
  }

  def size(fontSize: Int): RText = {
    font.size = fontSize
    this
  }

  def color(r: Int, g: Int, b: Int, opacity: Float = 1): RText = {
    font.color = RColor(r, g, b, opacity)
    this
  }

  def color(rColor: RColor): RText = {
    font.color = rColor
    this
  }

  def bold(): RText = {
    font.attribute = if (font.attribute == RFontAttribute.ITALIC) RFontAttribute.BOLD_ITALIC else RFontAttribute.BOLD
    this
  }

  def italic(): RText = {
    font.attribute = if (font.attribute == RFontAttribute.BOLD) RFontAttribute.BOLD_ITALIC else RFontAttribute.ITALIC
    this
  }

  def +(other: RText) = RTextList(ListBuffer(this, other))

  def plus(other: RText) = RTextList(ListBuffer(this, other))
}