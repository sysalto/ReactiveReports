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



package com.sysalto.render.basic

import com.sysalto.render.PdfDraw.DrawPoint
import com.sysalto.render.PdfNativeGenerator
import com.sysalto.report.reportTypes.RColor

object PdfBasic {
	def arc(center: DrawPoint, radius: Float, startAngle: Float, endAngle: Float): String = {
		val p0 = DrawPoint((center.x + radius * Math.cos(startAngle)).toFloat, (center.y + radius * Math.sin(startAngle)).toFloat)
		val lg = radius * 4 / 3.0 * Math.tan((endAngle - startAngle) * 0.25)
		val p1 = DrawPoint((p0.x - lg * Math.sin(startAngle)).toFloat, (p0.y + lg * Math.cos(startAngle)).toFloat)
		val p3 = DrawPoint((center.x + radius * Math.cos(endAngle)).toFloat, (center.y + radius * Math.sin(endAngle)).toFloat)
		val p2 = DrawPoint((p3.x + lg * Math.sin(endAngle)).toFloat, (p3.y - lg * Math.cos(endAngle)).toFloat)
		//https://stackoverflow.com/questions/1734745/how-to-create-circle-with-b%C3%A9zier-curves
		s"""${p1.x} ${p1.y} ${p2.x} ${p2.y} ${p3.x} ${p3.y} c \n"""
	}


	def circle(center: DrawPoint, radius: Float): String = {
		movePoint(center.x + radius, center.y) +
			arc(center, radius, 0, (Math.PI / 2.0).toFloat) +
			arc(center, radius, (Math.PI / 2.0).toFloat, Math.PI.toFloat) +
			arc(center, radius, Math.PI.toFloat, (3.0 * Math.PI / 2.0).toFloat) +
			arc(center, radius, (3.0 * Math.PI / 2.0).toFloat, 2 * Math.PI.toFloat)
	}

	def movePoint(point: DrawPoint): String = movePoint(point.x, point.y)

	def movePoint(x: Float, y: Float): String =s"""${x} ${y} m \n"""

	def lineTo(point: DrawPoint): String = lineTo(point.x, point.y)

	def lineTo(x: Float, y: Float): String =s"""${x} ${y} l \n"""

	def pattern(patternName: String): String = s"/Pattern cs /${patternName} scn"

	def convertColor(color: RColor): (Float, Float, Float) = {
		val r = color.r / 255f
		val g = color.g / 255f
		val b = color.b / 255f
		(r, g, b)
	}

	def border(borderColor: RColor): String = {
		val color = PdfNativeGenerator.convertColor(borderColor)
		s"${color._1} ${color._2} ${color._3} RG\n"
	}


	def fill(fillColor: RColor): String = {
		val color = PdfNativeGenerator.convertColor(fillColor)
		s"${color._1} ${color._2} ${color._3} rg\n"
	}

	def fillStroke(fill: Boolean, stroke: Boolean): String = {
		(fill, stroke) match {
			case (true, true) => "B\n"
			case (true, false) => "f\n"
			case (false, true) => "S\n"
			case _ => ""
		}
	}

	def rectangle(x: Float, y: Float, width: Float, height: Float): String =s"""${x} ${y} ${width} ${height} re \n"""

	val saveStatus: String = "q\n"
	val restoreStatus: String = "Q\n"
	val closePath:String="h\n"

}
