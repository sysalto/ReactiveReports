package com.sysalto.report

import com.sysalto.report.ReportTypes._
import com.sysalto.report.reportTypes.ReportColor

case class DirectDrawReport(val report: Report) {

	/**
		* custom drawing - move current point to x,y
		*
		* @param x
		* @param y
		*/
	def directDrawMovePoint(x: Float, y: Float): Unit = {
		assert(checkCoordinate(x, true))
		assert(checkCoordinate(y, false))
		val reportItem = new DirectDrawMovePoint(x, y)
		report.crtPage.items += reportItem
	}


	/**
		* custom drawing draw line from the currnt point to (x,y) and move the new current point to (x,y)
		*
		* @param x
		* @param y
		*/
	def directDrawLine(x: Float, y: Float): Unit = {
		assert(checkCoordinate(x, true))
		assert(checkCoordinate(y, false))
		val reportItem = new DirectDrawLine(x, y)
		report.crtPage.items += reportItem
	}


	/**
		* use custom drawing with pdf codes
		*
		* @param code
		*/
	def directDraw(code: String): Unit = {
		val reportItem = new DirectDraw(code)
		report.crtPage.items += reportItem
	}

	/**
		* custom draw circle with center (x,y) and radius
		*
		* @param x
		* @param y
		* @param radius
		*/
	def directDrawCircle(x: Float, y: Float, radius: Float): Unit = {
		assert(checkCoordinate(x, true))
		assert(checkCoordinate(y, false))
		assert(checkCoordinate(x + radius, true))
		assert(checkCoordinate(y + radius, false))
		val reportItem = new DirectDrawCircle(x, y, radius)
		report.crtPage.items += reportItem
	}


	/**
		* custom draw arc with center (x,y) ,radius from startAngle to endAngle
		*
		* @param x
		* @param y
		* @param radius
		* @param startAngle
		* @param endAngle
		*/
	def directDrawArc(x: Float, y: Float, radius: Float, startAngle: Float, endAngle: Float): Unit = {
		assert(checkCoordinate(x, true))
		assert(checkCoordinate(y, false))
		assert(checkCoordinate(x + radius, true))
		assert(checkCoordinate(y + radius, false))
		val reportItem = new DirectDrawArc(x, y, radius, startAngle, endAngle)
		report.crtPage.items += reportItem
	}

	/**
		* custom fill/stroke
		*
		* @param fill
		* @param stroke
		*/
	def directFillStroke(fill: Boolean, stroke: Boolean): Unit = {
		val reportItem = new DirectFillStroke(fill, stroke)
		report.crtPage.items += reportItem
	}


	/**
		* custom fill with a color
		*
		* @param reportColor
		*/
	def directDrawFill(reportColor: ReportColor): Unit = {
		val reportItem = new DirectDrawFill(reportColor)
		report.crtPage.items += reportItem
	}

	/**
		* custom close current path
		*/
	def directDrawClosePath(): Unit = {
		val reportItem = new DirectDrawClosePath()
		report.crtPage.items += reportItem
	}

	/**
		* custom stroke with a color
		*
		* @param reportColor
		*/
	def directDrawStroke(reportColor: ReportColor): Unit = {
		val reportItem = new DirectDrawStroke(reportColor)
		report.crtPage.items += reportItem
	}

	/**
		* custom draw rectangle from (x,y) having width and height
		*
		* @param x
		* @param y
		* @param width
		* @param height
		*/
	def directDrawRectangle(x1: Float, y1: Float, x2: Float, y2: Float): Unit = {
		assert(checkCoordinate(x1, true))
		assert(checkCoordinate(y1, false))
		assert(checkCoordinate(x2, true))
		assert(checkCoordinate(y2, false))

		val reportItem = new DirectDrawRectangle(x1, y1, x2, y2)
		report.crtPage.items += reportItem
	}


	private def checkCoordinate(coord: Float, isX: Boolean): Boolean = {
		if (isX) {
			coord >= 0 && coord <= report.pageLayout.width
		} else {
			coord >= 0 && coord <= report.pageLayout.height
		}

	}


	/**
		* draw a rounded rectange from (x1,y1) to (x2,y2) rounded with radius
		*
		* @param x1
		* @param y1
		* @param x2
		* @param y2
		* @param radius
		*/
	def roundRectangle(x1: Float, y1: Float, x2: Float, y2: Float, radius: Float) = {
		directDrawMovePoint(x1 + radius, y1)
		directDrawLine(x2 - radius, y1)
		directDrawArc(x2 - radius, y1 + radius, radius, (Math.PI * 0.5).toFloat, 0f)
		directDrawLine(x2, y2 - radius)
		directDrawArc(x2 - radius, y2 - radius, radius, 2 * Math.PI.toFloat, (3.0 * Math.PI * 0.5).toFloat)
		directDrawLine(x1 + radius, y2)
		directDrawArc(x1 + radius, y2 - radius, radius, (3.0 * Math.PI * 0.5).toFloat, Math.PI.toFloat)
		directDrawLine(x1, y1 + radius)
		directDrawArc(x1 + radius, y1 + radius, radius, Math.PI.toFloat, (Math.PI * 0.5).toFloat)

	}


}
