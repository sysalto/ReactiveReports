package com.sysalto.render

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
		s"""${p1.x} ${p1.y} ${p2.x} ${p2.y} ${p3.x} ${p3.y} c
     """.stripMargin
	}

	private def movePoint(point: DrawPoint): String =s"""${point.x} ${point.y} m"""

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
			val moveStr = movePoint(p0)
			val str1=arc(center, radius, 0, (Math.PI / 2.0).toFloat)
			val str2=arc(center,radius,  (Math.PI / 2.0).toFloat, Math.PI.toFloat)
			val str3=arc(center,radius, Math.PI.toFloat, (3.0*Math.PI / 2.0).toFloat)
			val str4=arc(center,radius, (3.0*Math.PI / 2.0).toFloat,2*Math.PI.toFloat)
			//https://stackoverflow.com/questions/1734745/how-to-create-circle-with-b%C3%A9zier-curves
			s"""${moveStr}
				 | ${str1}
				 | ${str2}
				 | ${str3}
				 | ${str4}
     """.stripMargin
		}
	}


	case class DrawStroke() extends PdfGraphicChuck {
		override def content: String = {
			"S"
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

}
