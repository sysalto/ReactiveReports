package com.sysalto.report

import com.sysalto.report.Implicits._
import com.sysalto.report.reportTypes.{RFont, ReportColor}

import scala.collection.JavaConverters._

class ReportChart(val report: Report) {

	private[this] class DrawPoint(val x: Float, val y: Float)

	private[this] val directDraw = new DirectDrawReport(report)


	def barChart(title: String, xLabel: String, yLabel: String, data: List[(String, ReportColor, Float)], x0: Float,
	             y0: Float, width: Float, height: Float, distance: Float): Unit = {
		val directDraw = new DirectDrawReport(report)
		assert(width > 0)
		assert(height > 0)
		assert(data.nonEmpty)
		assert(distance >= 0)
		val maxValue = data.map { case (x, _, value) => value }.max
		assert(maxValue > 0)
		assert(width - distance * data.length > 0)
		val vertScale = height / maxValue
		val itemWidth = (width - distance * data.length) / data.length
		var crtX = x0
		data.foreach {
			case (label, color, value) => {
				directDraw.rectangle(crtX, y0, crtX + itemWidth, (y0 - value * vertScale).toFloat)
				directDraw.setFillColor(color)
				directDraw.fill()
				val cell = ReportCell(label) centerAlign() inside ReportMargin(crtX, crtX + itemWidth)
				directDraw print(cell, y0 + 10)
				crtX += itemWidth + distance
			}
		}
		directDraw.movePoint(x0, y0)
		directDraw.lineTo(x0 + width + 20, y0)
		directDraw.movePoint(x0, y0)
		directDraw.lineTo(x0, y0 - height - 20)
		directDraw.stroke()
	}


	def pieChart(font: RFont, title: String, data: List[(String, ReportColor, Float)], x: Float, y: Float, width: Float, height: Float) = {
		def getPoint(center: DrawPoint, radius: Float, angle: Float): DrawPoint =
			new DrawPoint((center.x + radius * Math.cos(angle)).toFloat, (center.y - radius * Math.sin(angle)).toFloat)

		val total = data.map { case (_, _, value) => value }.sum
		val twoPI = 2.0 * Math.PI
		var initialAngle = (Math.PI * 0.5).toFloat
		var i = 0
		val angleList = data.map {
			case (key, color, value) => {
				val angleDif = (value / total * twoPI).toFloat
				val result = key -> (initialAngle, initialAngle - angleDif, color)
				i += 1
				initialAngle -= angleDif
				result
			}
		}
		val offset = 5.0f
		val radius = (Math.min(width, height) * 0.5).toFloat - offset
		val center = new DrawPoint(x + radius + offset, y + radius + offset)
		val str1 = angleList.foreach {
			case (label, (startAngle, endAngle, color)) => {
				val p1 = getPoint(center, radius, startAngle)
				directDraw.movePoint(center.x, center.y)
				directDraw.lineTo(p1.x, p1.y)
				directDraw.arc(center.x, center.y, radius, startAngle, endAngle)
				//				directDraw.lineTo(p2.x, p2.y)
				directDraw.closePath
				directDraw.setFillColor(color)
				directDraw.fillStroke(true, false)
			}
		}

		var ycrt = offset + 10
		val str2 = angleList.foreach {
			case (label, (startAngle, endAngle, color)) => {
				directDraw.rectangle(x + 2.0f * (radius + offset), y + ycrt, x + 10 + 2.0f * (radius + offset), y + ycrt + 10)
				directDraw.setFillColor(color)
				directDraw.fillStroke(true, false)
				report.text(ReportTxt(label, font).size(10), x + 2.0f * (radius + offset) + 20, y + ycrt + 8)
				ycrt += 12
			}
		}
		directDraw.roundRectangle(x, y, x + width, y + height, 5)
		directDraw.fillStroke(false, true)
	}


	/*
	For Java
	 */
	def pieChart(font: RFont, title: String, data: _root_.java.util.List[(String, ReportColor, Float)], x: Float, y: Float, width: Float, height: Float): Unit = {
		pieChart(font, title, data.asScala.toList, x, y, width, height)
	}

}
