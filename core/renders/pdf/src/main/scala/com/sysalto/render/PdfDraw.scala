package com.sysalto.render

import com.sysalto.report.reportTypes.RColor

object PdfDraw {

	abstract class PdfGraphicChuck {
		def content: String
	}

	case class DrawPoint(x: Double, y: Double)

	private def arc(center: DrawPoint, radius: Float, startAngle: Float, endAngle: Float): String = {
		val p0 = DrawPoint(center.x + radius * Math.cos(startAngle), center.y + radius * Math.sin(startAngle))
		val lg = radius * 4 / 3.0 * Math.tan((endAngle - startAngle) * 0.25)
		val p1 = DrawPoint(p0.x - lg * Math.sin(startAngle), p0.y + lg * Math.cos(startAngle))
		val p3 = DrawPoint(center.x + radius * Math.cos(endAngle), center.y + radius * Math.sin(endAngle))
		val p2 = DrawPoint(p3.x + lg * Math.sin(endAngle), p3.y - lg * Math.cos(endAngle))
		//https://stackoverflow.com/questions/1734745/how-to-create-circle-with-b%C3%A9zier-curves
		s"""${p1.x} ${p1.y} ${p2.x} ${p2.y} ${p3.x} ${p3.y} c \n"""
	}

	private def movePoint(point: DrawPoint): String =s"""${point.x} ${point.y} m \n"""

	private def lineTo(point: DrawPoint): String =s"""${point.x} ${point.y} l \n"""


	private def convertColor(color: RColor): (Float, Float, Float) = {
		val r = color.r / 255f
		val g = color.g / 255f
		val b = color.b / 255f
		(r, g, b)
	}

	case class DrawArc(center: DrawPoint, radius: Float, startAngle: Float, endAngle: Float) extends PdfGraphicChuck {
		override def content: String = {
			val p0 = DrawPoint(center.x + radius * Math.cos(startAngle), center.y + radius * Math.sin(startAngle))
			val moveStr = movePoint(p0)
			val arcStr = arc(center, radius, startAngle, endAngle)
			//https://stackoverflow.com/questions/1734745/how-to-create-circle-with-b%C3%A9zier-curves
			s"""${moveStr}
				 | ${arcStr}
     """.stripMargin
		}
	}

	case class DrawCircle(center: DrawPoint, radius: Float) extends PdfGraphicChuck {
		override def content: String = {
			val p0 = DrawPoint(center.x + radius, center.y)
			val str = movePoint(p0) +
				arc(center, radius, 0, (Math.PI / 2.0).toFloat) +
				arc(center, radius, (Math.PI / 2.0).toFloat, Math.PI.toFloat) +
				arc(center, radius, Math.PI.toFloat, (3.0 * Math.PI / 2.0).toFloat) +
				arc(center, radius, (3.0 * Math.PI / 2.0).toFloat, 2 * Math.PI.toFloat)
			//https://stackoverflow.com/questions/1734745/how-to-create-circle-with-b%C3%A9zier-curves
			s"""${str}"""
		}
	}


	case class DrawStroke() extends PdfGraphicChuck {
		override def content: String = {
			"S"
		}
	}

	case class DrawFill() extends PdfGraphicChuck {
		override def content: String = {
			"f"
		}
	}

	case class DrawFillStroke() extends PdfGraphicChuck {
		override def content: String = {
			"B"
		}
	}

	case class DrawMovePoint(x: Float, y: Float) extends PdfGraphicChuck {
		override def content: String = {
			s"""${x} ${y} m"""
		}
	}

	case class DrawLine(x: Float, y: Float) extends PdfGraphicChuck {
		override def content: String = {
			s"""${x} ${y} l"""
		}
	}

	case class DrawRectangle(x: Float, y: Float, width: Float, height: Float) extends PdfGraphicChuck {
		override def content: String = {
			s"""${x} ${y} ${width} ${height} re"""
		}
	}

	case class DrawBorderColor(borderColor: RColor) extends PdfGraphicChuck {
		override def content: String = {
			val color = convertColor(borderColor)
			s"${color._1} ${color._2} ${color._3} RG"
		}
	}

	case class DrawFillColor(borderColor: RColor) extends PdfGraphicChuck {
		override def content: String = {
			val color = convertColor(borderColor)
			s"${color._1} ${color._2} ${color._3} rg"
		}
	}

	case class DrawPattern(pdfPattern: PdfGPattern) extends PdfGraphicChuck {
		override def content: String = {
			s"/Pattern cs /${pdfPattern.name} scn"
		}
	}

	case class DrawRoundRectangle(x1: Float, y1: Float, x2: Float, y2: Float, radius: Float) extends PdfGraphicChuck {
		override def content: String = {
			movePoint(DrawPoint(x1 + radius, y1)) +
				lineTo(DrawPoint(x2 - radius, y1)) +
				movePoint(DrawPoint(x2, y1 - radius)) +
				arc(DrawPoint(x2 - radius, y1 - radius), radius, 0f, (Math.PI * 0.5).toFloat) +
				movePoint(DrawPoint(x2, y1 - radius)) +
				lineTo(DrawPoint(x2, y2 + radius)) +
				movePoint(DrawPoint(x2 - radius, y2)) +
				arc(DrawPoint(x2 - radius, y2 + radius), radius, (3.0 * Math.PI * 0.5).toFloat, 2 * Math.PI.toFloat) +
				movePoint(DrawPoint(x2 - radius, y2)) +
				lineTo(DrawPoint(x1 + radius, y2))+
				movePoint(DrawPoint(x1, y2 +radius)) +
				arc(DrawPoint(x1 + radius, y2 + radius), radius, Math.PI.toFloat,(3.0 * Math.PI * 0.5).toFloat) +
				movePoint(DrawPoint(x1, y2 +radius))+
				lineTo(DrawPoint(x1, y1-radius))+
				movePoint(DrawPoint(x1+radius, y1))+
				arc(DrawPoint(x1 + radius, y1-radius ), radius,(Math.PI*0.5).toFloat, Math.PI.toFloat)
			//	s"/Pattern cs /${pdfPattern.name} scn"
		}
	}

}
