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


package com.sysalto.report


import com.sysalto.report.util.{PdfFactory, PersistenceFactory, PersistenceUtil, RockDbUtil}

import scala.collection.mutable.ListBuffer
import ReportTypes._
import com.sysalto.report.reportTypes._
import com.sysalto.report.function.{RConsumer1, RConsumer2, RFunction1}
import com.sysalto.report.util.serializers.ObjectSerialization

import scala.annotation.varargs

/** Report class- for Scala
  *
  * @param name        - name of the pdf file. It should include the pdf extension
  * @param orientation - report's orientation:PORTRAIT or LANDSCAPE.
  * @param pdfFactory  - the pdfFactory variable.This is needed for report to delegate all the report's call to this implementation.
  */
class Report(val name: String, val orientation: ReportPageOrientation.Value = ReportPageOrientation.PORTRAIT, val pageFormat: ReportPageFormat = LetterFormat,
             val persistence: PersistenceFactory = null, val pdfCompression: Boolean = true, tagged: Boolean = true)(implicit pdfFactory: PdfFactory) {
  private[this] var pageNbrs = 1L
  private[this] var crtPageNbr = 1L
  private[report] val crtPage = new ReportPage(new ListBuffer[ReportItem]())
  var font = RFont(10, "Helvetica")
  private[this] var simulation = false
  private[report] val pdfUtil = pdfFactory.getPdf


  private[this] var persistenceFactory: PersistenceFactory = persistence
  private[this] var persistenceUtil: PersistenceUtil = null
  private[this] var crtYPosition = 0f
  private[this] var lastPosition: ReportPosition = new ReportPosition(0, 0)

  /**
    * Function that is call when the total number of pages is needed at before the end of the report. This doesn't apply to page header/footer.
    * Use it for example when you need summary at the beging of the report
    *
    * @param value
    */
  def setSimulation(value: Boolean): Unit = {
    simulation = value
    crtYPosition = pdfUtil.pgSize.height - setHeaderSize(crtPageNbr)
  }

  /** header callback
    * first param - current page
    * second param - total number of pages
    */
  var headerFct: (java.lang.Long, java.lang.Long) => Unit = {
    case (_, _) =>
  }

  /**
    * new page callback.
    * It's called fisrt time after new page - usefull for drawing backgroun images.
    */
  var newPageFct: (java.lang.Long) => Unit = null

  /** footer callback
    * first param - current page
    * second param - total number of pages
    */
  var footerFct: (java.lang.Long, java.lang.Long) => Unit = {
    case (_, _) =>
  }
  /** set size of the header
    * param - current page - can be use to set up header size only for some pages -
    * for example if (crtPage==0) return 0 -> no header for the first page.
    *
    * @return size of the header - by default 0 (no header)
    */
  var setHeaderSize: java.lang.Long => java.lang.Float = { _ => 0 }

  /** set size of the footer
    * param - current page - can be use to set up footer size only for some pages -
    * for example if (crtPage==0) return 0 -> no header for the first page.
    *
    * @return size of the header - by default 0 (no header)
    */
  var setFooterSize: java.lang.Long => java.lang.Float = { _ => 0 }

  /**
    *
    * @return the current page number of the report.
    */
  def getCrtPageNbr() = crtPageNbr

  private[this] def saveCrtPage(): Unit = {
    writePage(crtPageNbr, crtPage)
    crtPage.items.clear()
  }

  private[this] def switchPages(newPage: Long): Unit = {
    if (newPage > pageNbrs + 1) {
      throw new Exception(s"$newPage > ${pageNbrs + 1}")
    }
    if (newPage == crtPageNbr) {
      return
    }
    saveCrtPage()
    if (newPage <= pageNbrs) {
      val pageOpt = readPage(newPage)
      if (pageOpt.isDefined) {
        crtPage.items.appendAll(pageOpt.get.items)
      } else {
        if (newPageFct != null) {
          newPageFct(newPage)
        }
      }
    }
    crtPageNbr = newPage
    if (newPage > pageNbrs) {
      pageNbrs = newPage
    }
    crtYPosition = pdfUtil.pgSize.height - setHeaderSize(newPage)
    if (lastPosition < getCurrentPosition) {
      lastPosition = getCurrentPosition
    }
  }

  private[this] def newPageInternal(): Unit = {
    pdfUtil.newPage()
  }


  private[report] def reportWrap(text: List[ReportTxt], x0: Float, y0: Float, x1: Float, y1: Float,
                                 wrapAlign: WrapAlign.Value, simulate: Boolean = false): Option[WrapBox] = {

    pdfUtil.wrap(text, x0, y0, x1, y1, wrapAlign, simulate, lineHeight)
  }

  /*
  Returns numbers of line per page
   */
  def linesPerPage: Int = (pdfUtil.pgSize.height / lineHeight).toInt

  /*
  Returns line's height
   */
  def lineHeight: Float = (font.size * 1.5).toFloat

  /*
  keep page size
   */
  lazy val pageLayout: Rectangle = pdfUtil.pgSize

  /*
  Returns report's current position (page number and vertical position on page)
   */
  def getCurrentPosition: ReportPosition = new ReportPosition(crtPageNbr, crtYPosition)

  /*
    GoTo the last known position in the report
     */
  def gotoLastPosition(): Unit = {
    setCurrentPosition(lastPosition)
  }


  /*
  Restore a report position (page and vertical position on page)
   */
  def setCurrentPosition(position: ReportPosition): Unit = {
    switchPages(position.pageNbr)
    crtYPosition = position.y
  }

  /*
    Convert line number to  position
   */
  def toY(line: Int): Float = line * lineHeight

  /*
  convert position to line
   */
  def toLine(y: Float): Int = ((pdfUtil.pgSize.height - y) / lineHeight).toInt


  /*
  set vertical position on current page
   */
  def setYPosition(y: Float): Unit = {
    crtYPosition = pdfUtil.pgSize.height - y
  }

  private[report] def getYPosition(y: Float) = pdfUtil.pgSize.height - y

  private[report] def getYPosition = crtYPosition

  /*
  set vertical position on current page to the line position
   */
  def setCrtLine(line: Int): Unit = {
    setYPosition(line * lineHeight)
  }

  /*
  Returns the vertical position on current page - position 0 is on the top of the page
   */
  def getY: Float = pdfUtil.pgSize.height - crtYPosition

  /*
  Go to the next page (create a new one if necessary)
   */
  def nextPage(): Unit = {
    if (simulation) {
      crtYPosition = pdfUtil.pgSize.height - setHeaderSize(pageNbrs + 1)
      return
    }
    val newPage = if (crtPageNbr < pageNbrs) crtPageNbr + 1 else {
      pageNbrs += 1
      pageNbrs
    }
    try {
      switchPages(newPage)

    } catch {
      case e: Throwable =>
        e.printStackTrace()
    }
  }


  /*
  go to the next lineNbr line
   */
  def nextLine(lineNbr: Int): Unit = {
    crtYPosition = crtYPosition - lineNbr * lineHeight
    if (lastPosition < getCurrentPosition) {
      lastPosition = getCurrentPosition
    }
  }


  /*
  Draws rectangle from (x1,y1) to (x2,y2) with optional radius and color
  x1,x2 - horizontal coordinates
  y1,y2 - vertical coordinates  with 0 starting from the top
   */
  def drawRectangle(x1: Float, y1: Float, x2: Float, y2: Float, radius: Float = 0, color: Option[ReportColor] = None, fillColor: Option[ReportColor] = None): Unit = {
    val reportItem = new ReportRectangle(x1, y1, x2, y2, radius, color, fillColor)
    crtPage.items += reportItem
  }


  /*
    Draws a line between (x1,y1) and (x2,y2) with thickness lineWidth and color
    x are horizontal coordinates and y vertical starting with 0 from the top
   */
  def line(x1: Float = 0, y1: Float = getY, x2: Float = -1, y2: Float = -1, lineWidth: Float = 1, color: ReportColor = ReportColor(0, 0, 0),
           lineDashType: Option[LineDashType] = None): Unit = {
    val reportItem = new ReportLine(x1, y1, x2, y2, lineWidth, color, lineDashType)
    crtPage.items += reportItem
  }

  /*
    Draws txt at (x,y)
    By default y is the current y
   */
  def text(txt: ReportTxt, x: Float, y: Float = -1): Unit = {
    if (simulation) {
      return
    }
    val y1 = if (y == -1) getY else y
    if (txt.font.fontName.isEmpty) {
      txt.font.fontName = this.font.fontName
      txt.font.externalFont = this.font.externalFont
    }
    val reportItem = new ReportText(txt, x, y1)
    crtPage.items += reportItem
  }

  /*
  Draw text align at index and position (x,y).
  By default y is current y.
   */
  def textAligned(txt: ReportTxt, index: Int, x: Float, y: Float = -1): Unit = {
    val y1 = if (y == -1) getY else y
    if (txt.font.fontName.isEmpty) {
      txt.font.fontName = this.font.fontName
      txt.font.externalFont = this.font.externalFont
    }
    val reportItem = new ReportTextAligned(txt, x, y1, index)
    crtPage.items += reportItem
  }

  /*
  close the report.
   */
  private[this] def close(): Unit = {
    pdfUtil.close()
    persistenceUtil.close()
  }


  /*
  Draw a bar chart with title,xLabel,yLabel and  data from (x0,y0) with width and height dimensions.
  See jfreechart for details.
 */
  def drawBarChart(title: String, xLabel: String, yLabel: String, data: List[(Double, String, String)], x0: Float, y0: Float, width: Float, height: Float): Unit = {
    crtPage.items += new ReportBarChart(title, xLabel, yLabel, data, x0, y0, width, height)
  }

  /*
  Draw image from file at (x,y) with width and height having opacity.
  opacity is betwwen 0 (full transparent) and 1 (full opaque)
 */
  def drawImage(file: String, x: Float, y: Float, width: Float, height: Float, opacity: Float): Unit = {
    crtPage.items += new ReportImage(file, x, y, width, height, opacity)
  }

  /*
  Write the text in wrap mode in a box from (x0,y0) to (x1,y1) with wrapOption and wrap align.
  There are two types of invocations:
    simulate =true only calculate the new coordinates
    simulate=false write the text
    This is a low level function.
    Easy way is to use [RRow]
   */
  def wrap(text: List[ReportTxt], x0: Float, y0: Float, x1: Float, y1: Float,
           wrapAlign: WrapAlign.Value = WrapAlign.WRAP_LEFT, simulate: Boolean = false): Option[WrapBox] = {
    val text1 = text.map(item => {
      val font = if (item.font.fontName.isEmpty) {
        val font1 = item.font
        font1.fontName = this.font.fontName
        font1.externalFont = this.font.externalFont
        font1
      } else {
        item.font
      }
      ReportTxt(item.txt, font)
    })
    assert(x1 - x0 > 0)
    if (simulate) {
      reportWrap(text1, x0, y0, x1, y1, wrapAlign, simulate)
    } else {
      val reportItem = new ReportTextWrap(text1, x0, y0, x1, y1, wrapAlign)
      crtPage.items += reportItem
      None
    }
  }


  /*
  set default report font size
   */
  def setFontSize(size: Int): Unit = {
    font.size = size
  }

  /*
    draw a rectangle with vertical shade between 'from' and 'to' colors.
   */
  def verticalShade(rectangle: DRectangle, from: ReportColor, to: ReportColor): Unit = {
    crtPage.items += new ReportVerticalShade(rectangle, from, to)
  }


  /*
  render the report.
   */
  def render(): Unit = {
    saveCrtPage()
    // call header and footer handler
    // pageNbrs=1
    for (i <- 1L to pageNbrs) {
      if (setHeaderSize(i) > 0 || setFooterSize(i) > 0) {
        val page = readPage(i).get
        crtPage.items.clear()
        crtPage.items.appendAll(page.items)
        if (setHeaderSize(i) > 0) {
          headerFct(i, pageNbrs)
        }
        if (setFooterSize(i) > 0) {
          footerFct(i, pageNbrs)
        }
        writePage(i, crtPage)
      }
    }
    pdfUtil.setPagesNumber(pageNbrs)
    for (i <- 1L to pageNbrs) {
      val page = readPage(i)
      if (page.isDefined) {
        val last = i == pageNbrs
        page.get.items.foreach(item => item.render(this))
        if (!last) {
          newPageInternal()
        }
      }
    }
    close()
  }


  /*
  number of lines left on current page.
   */
  def lineLeft: Int = (crtYPosition / lineHeight).toInt

  /*
  print a cell (means wrapping text).
   */
  def print(cell: ReportCell): BoundaryRect = {
    wrap(cell.txt, cell.margin.left, getY, cell.margin.right, Float.MaxValue, cell.align)
    new BoundaryRect(cell.margin.left, getYPosition - 2, cell.margin.right + 2, getYPosition + lineHeight - 4)
  }

  /*
  print a RText (no wrapping).
   */
  def print(txt: ReportTxt): TextDsl = {
    if (txt.font.fontName.isEmpty) {
      txt.font.fontName = this.font.fontName
      txt.font.externalFont = this.font.externalFont
    }
    val result = new TextDsl(this, txt)
    result
  }

  /**
    * print row of a table defined by List[ReportCell]
    *
    * @param cells
    * @param cellAlign
    * @param top
    * @param bottom
    */
  def print(cells: List[ReportCell], cellAlign: CellAlign = CellAlign.TOP, top: Float = 0, bottom: Float = 0): Unit = {
    cellAlign match {
      case CellAlign.TOP => {
        val y = getY
        cells.foreach(cell => {
          wrap(cell.txt, cell.margin.left, y, cell.margin.right, Float.MaxValue, cell.align)
        })
        setYPosition(y)
      }
      case c@(CellAlign.BOTTOM | CellAlign.CENTER) => {
        val y = getY
        val wrapList = calculateWrapList(cells)
        if (c == CellAlign.CENTER) {
          val middle = (top + bottom) * 0.5
          cells.zipWithIndex.foreach {
            case (cell, index) => {
              val wrapBox = wrapList(index)
              val fontHeight = cell.txt.head.font.size
              val height = wrapBox.currentY - wrapBox.initialY + wrapBox.textHeight
              val y1 = (middle - height * 0.5 + wrapBox.textHeight).toFloat
              wrap(cell.txt, cell.margin.left, y1, cell.margin.right, Float.MaxValue, cell.align)
            }
          }
        } else {
          cells.zipWithIndex.foreach {
            case (cell, index) => {
              val wrapBox = wrapList(index)
              val fontHeight = cell.txt.head.font.size
              val height = wrapBox.currentY - wrapBox.initialY + wrapBox.textHeight
              val y1 = bottom - height + +wrapBox.textHeight
              wrap(cell.txt, cell.margin.left, y1, cell.margin.right, Float.MaxValue, cell.align)
            }
          }
        }

        setYPosition(y)
      }
    }

  }


  // java wrappers
  @varargs def print(cellAlign: CellAlign, top: Float, bottom: Float, cells: ReportCell*): Unit = print(cells.toList, cellAlign, top, bottom)

  @varargs def print(cells: ReportCell*): Unit = print(cells.toList)

  /*
  function for use with report DSL to print a line.
   */
  def line(): LineDsl = {
    new LineDsl(this)
  }

  /*
  only calculate a cell.
   */
  def calculate(cell: ReportCell): WrapBox = {
    wrap(cell.txt, cell.margin.left, getY, cell.margin.right, Float.MaxValue, cell.align, simulate = true).get
  }

  /**
    * get the  width of a list of cells (row)
    *
    * @param cells
    * @return
    */
  def calculate(cells: List[ReportCell]): Float = {
    val yPosList = calculateWrapList(cells).map(wrap => wrap.currentY)
    yPosList.reduceLeft((f1, f2) => if (f1 > f2) f1 else f2)
  }

  private[this] def calculateWrapList(cells: List[ReportCell]): List[WrapBox] = {
    val y = getY
    val wrapList = cells.map(cell => {
      wrap(cell.txt, cell.margin.left, y, cell.margin.right, Float.MaxValue, cell.align, simulate = true).get
    })
    setYPosition(y)
    wrapList
  }


  @varargs def calculate(cells: ReportCell*): Float = calculate(cells.toList)

  /*
  draw a rectamgle using dsl
   */
  def rectangle(): RectangleDsl = {
    val reportDsl = new RectangleDsl(this)
    reportDsl
  }


  // The next three functions are for implementing keep band together

  /*
  get a checkpoint for further cut and paste.
   */
  def checkpoint(): ReportCheckpoint = new ReportCheckpoint(crtPage.items.length, getY)

  /*
  cut all the items from the reportCheckpoint to the current
   */
  def cut(reportCheckpoint: ReportCheckpoint): ReportCut = {
    val nbr = reportCheckpoint.itemPos
    val result = crtPage.items.drop(nbr)
    val newList = crtPage.items.take(nbr)
    crtPage.items.clear()
    crtPage.items ++= newList
    new ReportCut(getY, result.toSeq)
  }

  /*
  paste the reportCheckpoint into current page.
   */
  def paste(reportCheckpoint: ReportCheckpoint, reportCut: ReportCut): Unit = {
    if (reportCut.list.nonEmpty) {
      reportCut.list.foreach(item => item.update(reportCheckpoint.yCrt - getY))
      crtPage.items ++= reportCut.list
      setYPosition(getY + reportCut.yCrt - reportCheckpoint.yCrt)
      if (lastPosition < getCurrentPosition) {
        lastPosition = getCurrentPosition
      }
    }
  }

  /*
  Insert a new number pages before pageNbr
  */
  def insertPages(number: Long, pageNbr: Long): Unit = {
    saveCrtPage()
    for (i <- pageNbrs to pageNbr by -1) {
      val page = readPage(i)
      if (page.isDefined) {
        writePage(i + number, page.get)
      }
    }
    pageNbrs += number
    crtPageNbr = pageNbr
    for (i <- pageNbr to pageNbr + number - 1) {
      val emptyPage = new ReportPage(ListBuffer[ReportItem]())
      writePage(i, emptyPage)
    }
    crtYPosition = pdfUtil.pgSize.height - setHeaderSize(pageNbr)


  }


  // java compatibility


  def newPageFctCallback(fct: RConsumer1[java.lang.Long]): Unit = {
    newPageFct = { pgNbr =>
      fct.apply(pgNbr)
    }
  }

  def headerSizeCallback(fct: RFunction1[java.lang.Long, java.lang.Float]): Unit = {
    setHeaderSize = { pgNbr =>
      fct.apply(pgNbr)
    }
  }


  def footerSizeCallback(fct: RFunction1[java.lang.Long, java.lang.Float]): Unit = {
    setFooterSize = { pgNbr =>
      fct.apply(pgNbr)
    }
  }

  def headerFct(fct: RConsumer2[java.lang.Long, java.lang.Long]): Unit = {
    headerFct = {
      case (pgNbr, pgMax) =>
        fct.apply(pgNbr, pgMax)
    }
  }


  def newPageFct(fct: RConsumer1[java.lang.Long]): Unit = {
    newPageFct = {
      case (pgNbr) =>
        fct.apply(pgNbr)
    }
  }

  def footerFct(fct: RConsumer2[java.lang.Long, java.lang.Long]): Unit = {
    footerFct = {
      case (pgNbr, pgMax) =>
        fct.apply(pgNbr, pgMax)
    }
  }

  def nextLine(): Unit = {
    nextLine(1)
  }

  def text(txt: String, x: Float, y: Float): Unit = {
    text(ReportTxt(txt), x, y)
  }

  def text(txt: String, x: Float): Unit = {
    text(ReportTxt(txt), x)
  }


  def drawImage(file: String, x: Float, y: Float, width: Float, height: Float): Unit = {
    drawImage(file, x, y, width, height, 1f)
  }

  def setExternalFont(externalFont: RFontFamily): Unit = {
    pdfUtil.setExternalFont(externalFont)
  }

  def setLinkToPage(boundaryRect: BoundaryRect, pageNbr: Long, left: Int = 0, top: Int = 0): Unit = {
    val reportLink = new ReportLinkToPage(boundaryRect, pageNbr, left, top)
    crtPage.items += reportLink
  }

  def setLinkToUrl(boundaryRect: BoundaryRect, url: String): Unit = {
    val reportLink = new ReportLinkToUrl(boundaryRect, url)
    crtPage.items += reportLink
  }

  def getTextWidth(txt: ReportTxt): java.lang.Float = {
    val txt1 = if (txt.font.fontName.isEmpty) {
      ReportTxt(txt.txt, this.font)
    } else txt
    pdfUtil.getTextWidth(txt1)
  }

  def getTextWidth(cell: ReportCell): List[Float] = pdfUtil.getTextWidth(cell)

  def getTextWidthJ(cell: ReportCell): java.util.List[java.lang.Float] = ReportCommon.asJava(pdfUtil.getTextWidth(cell).map(item => item.asInstanceOf[java.lang.Float]))

  def start(): Unit = {
    if (newPageFct != null) {
      newPageFct(1)
    }
  }

  def writePage(pageNbr: Long, page: ReportPage): Unit = persistenceUtil.writeObject(pageNbr, ObjectSerialization.serialize(page))

  def readPage(pageNbr: Long): Option[ReportPage] = {
    val bytes = persistenceUtil.readObject(pageNbr)
    if (bytes == null) {
      None
    } else {
      Some(ObjectSerialization.deserialize[ReportPage](bytes))
    }
  }

  // class initialize
  if (persistenceFactory == null) {
    persistenceFactory = new PersistenceFactory() {
      override def getPersistence(): PersistenceUtil = {
        val dbFolder = System.getProperty("java.io.tmpdir")
        val prefix = "persistence"
        val extension = ".db"
        new RockDbUtil(prefix, extension, dbFolder)
      }
    }
  }
  pdfUtil.open(name, orientation, pageFormat, persistenceFactory, pdfCompression)
  crtYPosition = pdfUtil.pgSize.height
  if (lastPosition < getCurrentPosition) {
    lastPosition = getCurrentPosition
  }

  persistenceUtil = persistenceFactory.open()

}

object Report {
  def apply(name: String, orientation: ReportPageOrientation.Value = ReportPageOrientation.PORTRAIT,
            pageFormat: ReportPageFormat = LetterFormat, persistence: PersistenceFactory = null, pdfCompression: Boolean = true, tagged: Boolean = true)
           (implicit pdfFactory: PdfFactory): Report = {
    new Report(name, orientation, pageFormat, persistence, pdfCompression)(pdfFactory)
  }

  /** Static method to create a new report from Java
    *
    * @param name        - name of the pdf file. It should include the pdf extension
    * @param orientation - report's orientation:PORTRAIT or LANDSCAPE.
    * @param pdfFactory  - the pdfFactory variable.This is needed for report to delegate all the report's call to this implementation.
    * @return the new report
    */
  def create(name: String, orientation: ReportPageOrientation.Value, pdfFactory: PdfFactory,
             pageFormat: ReportPageFormat, persistence: PersistenceFactory): Report = {
    new Report(name, orientation, pageFormat, persistence)(pdfFactory)
  }

  def create(name: String, orientation: ReportPageOrientation.Value, pdfFactory: PdfFactory): Report = {
    new Report(name, orientation, null)(pdfFactory)
  }
}
