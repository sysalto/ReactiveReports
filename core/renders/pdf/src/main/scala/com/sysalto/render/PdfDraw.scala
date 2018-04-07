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

import com.sysalto.report.reportTypes.{LineDashType, RFont, ReportColor}
import com.sysalto.render.basic.PdfBasic._
import PdfChart._
import com.sysalto.render.serialization.RenderReport

object PdfDraw {

	abstract class PdfGraphicFragment {
		def updateContent(renderReport: RenderReport): Unit = {

		}

		def content: String
	}

	class DrawPoint(val x: Float, val y: Float)


	def roundRectangle(x1: Float, y1: Float, x2: Float, y2: Float, radius: Float): String = {
		movePoint(new DrawPoint(x1 + radius, y1)) +
			lineTo(new DrawPoint(x2 - radius, y1), 1) +
			arc(new DrawPoint(x2 - radius, y1 - radius), radius, (Math.PI * 0.5).toFloat, 0f) +
			lineTo(new DrawPoint(x2, y2 + radius), 1) +
			arc(new DrawPoint(x2 - radius, y2 + radius), radius, 2 * Math.PI.toFloat, (3.0 * Math.PI * 0.5).toFloat) +
			lineTo(new DrawPoint(x1 + radius, y2), 1) +
			arc(new DrawPoint(x1 + radius, y2 + radius), radius, (3.0 * Math.PI * 0.5).toFloat, Math.PI.toFloat) +
			lineTo(new DrawPoint(x1, y1 - radius), 1) +
			arc(new DrawPoint(x1 + radius, y1 - radius), radius, Math.PI.toFloat, (Math.PI * 0.5).toFloat) +
			closePath
	}


	class DrawArc(center: DrawPoint, radius: Float, startAngle: Float, endAngle: Float) extends PdfGraphicFragment {
		override def content: String = {
			val p0 = new DrawPoint((center.x + radius * Math.cos(startAngle)).toFloat, (center.y + radius * Math.sin(startAngle)).toFloat)
			val moveStr = movePoint(p0)
			val arcStr = arc(center, radius, startAngle, endAngle)
			s"""${moveStr}
				 | ${arcStr}
     """.stripMargin
		}
	}

	class DrawCircle(center: DrawPoint, radius: Float) extends PdfGraphicFragment {
		override def content: String = circle(center, radius)
	}


	class DrawStroke() extends PdfGraphicFragment {
		override def content: String = {
			"S"
		}
	}

	class DrawFill() extends PdfGraphicFragment {
		override def content: String = {
			"f"
		}
	}

	class DrawFillStroke() extends PdfGraphicFragment {
		override def content: String = {
			"B"
		}
	}

	class DrawMovePoint(x: Float, y: Float) extends PdfGraphicFragment {
		override def content: String = {
			s"""${x} ${y} m"""
		}
	}

	class DrawLine(val x1: Float, val y1: Float, val x2: Float, val y2: Float, val vlineWidth: Float, val color: ReportColor, lineDashType: Option[LineDashType]) extends PdfGraphicFragment {
		override def content: String = {
			saveStatus + movePoint(x1, y1) + lineWidth(vlineWidth) +
				(if (lineDashType.isDefined) lineDash(lineDashType.get) else "") +
				lineTo(x2, y2) + border(color) + fillStroke(false, true) + restoreStatus
		}
	}

	class DrawRectangle(x: Float, y: Float, width: Float, height: Float) extends PdfGraphicFragment {
		override def content: String = {
			s"""${x} ${y} ${width} ${height} re"""
		}
	}

	class DrawBorderColor(borderColor: ReportColor) extends PdfGraphicFragment {
		override def content: String = {
			val color = convertColor(borderColor)
			s"${color._1} ${color._2} ${color._3} RG"
		}
	}

	class DrawFillColor(borderColor: ReportColor) extends PdfGraphicFragment {
		override def content: String = {
			val color = convertColor(borderColor)
			s"${color._1} ${color._2} ${color._3} rg"
		}
	}


}
