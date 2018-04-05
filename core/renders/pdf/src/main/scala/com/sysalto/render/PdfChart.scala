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


package com.sysalto.render

import com.sysalto.render.PdfDraw._
import com.sysalto.report.reportTypes.{RFont, ReportColor, ReportTxt}
import com.sysalto.render.basic.PdfBasic._
import com.sysalto.render.serialization.RenderReport

object PdfChart {

	private[this] val rnd = new scala.util.Random

	private[this] def randomColor(): ReportColor = {
		def hsvToRgb(h: Double, s: Double, v: Double): ReportColor = {
			val h1 = (h * 6).floor.toInt
			val f = h * 6 - h1
			val p = v * (1 - s)
			val q = v * (1 - f * s)
			val t = v * (1 - (1 - f) * s)
			val (r, g, b) = h1 match {
				case 0 => (v, t, p)
				case 1 => (q, v, p)
				case 2 => (p, v, t)
				case 3 => (p, q, v)
				case 4 => (t, p, v)
				case 5 => (v, p, q)
			}
			ReportColor((r * 256).toInt, (g * 256).toInt, (b * 256).toInt)
		}

		val goldenRatio = 0.618033988749895

		val h = rnd.nextDouble()
		hsvToRgb((h + goldenRatio) % 1, 1, 0.95)
	}

	private[this] def getColor(i: Int, total: Int): ReportColor = ReportColor((256.0 * i / total).toInt, (256.0 * (256.0 - i) / total).toInt, (256.0 * (256.0 - i) / total).toInt)


	def pieChart1(renderReport: RenderReport, font: RFont, title: String, data: List[(String, Double)], x: Float, y: Float, width: Float, height: Float): String = {
		def getPoint(center: DrawPoint, radius: Float, angle: Float): DrawPoint =
			DrawPoint((center.x + radius * Math.cos(angle)).toFloat, (center.y + radius * Math.sin(angle)).toFloat)

		val total = (data.map { case (key, value) => value }).sum
		val twoPI = 2.0 * Math.PI
		var initialAngle = (Math.PI * 0.5).toFloat
		var i = 0
		val angleList = data.map {
			case (key, value) => {
				val angleDif = (value / total * twoPI).toFloat
				val result = (key -> (initialAngle, initialAngle - angleDif, getColor(i, data.length)))
				i += 1
				initialAngle -= angleDif
				result
			}
		}
		val offset = 5.0f
		val radius = (Math.min(width, height) * 0.5).toFloat - offset
		val center = DrawPoint(x + radius + offset, y - radius - offset)
		val str1 = angleList.map { case (label, (startAngle, endAngle, color)) => {
			val p1 = getPoint(center, radius, startAngle)
			val p2 = getPoint(center, radius, endAngle)
			movePoint(center) +
				lineTo(p1, 1) +
				arc(center, radius, startAngle, endAngle) +
				lineTo(p2, 1) +
				closePath +
				fill(color) +
				fillStroke(true, false)
		}
		}.mkString("")

		var ycrt = offset + 10
		val str2 = angleList.map {
			case (label, (startAngle, endAngle, color)) => {
				val s = rectangle(x + 2.0f * (radius + offset), y - ycrt, 10, 10) + fill(color) + fillStroke(true, false)
				renderReport.text(x + 2.0f * (radius + offset) + 20, y - ycrt + 1, ReportTxt(label, font).size(10))
				ycrt += 12
				s
			}
		}.mkString("")
		roundRectangle(x, y, x + width, y - height, 5) + fillStroke(false, true) + str1 + str2
	}


}
