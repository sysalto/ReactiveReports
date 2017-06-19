package com.sysalto.render

import com.sysalto.render.PdfDraw._
import com.sysalto.report.reportTypes.RColor
import com.sysalto.render.basic.PdfBasic._

object PdfChart {

	//http://martin.ankerl.com/2009/12/09/how-to-create-random-colors-programmatically/
	val rnd = new scala.util.Random

	private def randomColor(): RColor = {
		def hsvToRgb(h: Double, s: Double, v: Double): RColor = {
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
			RColor((r * 256).toInt, (g * 256).toInt, (b * 256).toInt)
		}

		val goldenRatio = 0.618033988749895

		val h = rnd.nextDouble()
		hsvToRgb((h + goldenRatio) % 1, 1, 0.95)
	}

	def pieChart(title: String, data: List[(String, Double)], x: Float, y: Float, width: Float, height: Float): String = {
		def getPoint(center: DrawPoint, radius: Float, angle: Float): DrawPoint =
			DrawPoint((center.x + radius * Math.cos(angle)).toFloat, (center.y + radius * Math.sin(angle)).toFloat)

		val total = (data.map { case (key, value) => value }).sum
		val twoPI = 2.0 * Math.PI
		var initialAngle = (Math.PI * 0.5).toFloat
		val angleList = data.map {
			case (key, value) => {
				val angleDif = (value / total * twoPI).toFloat
				val result = (key -> (initialAngle, initialAngle + angleDif, randomColor()))
				initialAngle += angleDif
				result
			}
		}
		val offset=5.0f
		val radius = (Math.min(width, height) * 0.5).toFloat-offset
		val center = DrawPoint(x + radius+offset, y - radius-offset)
		val str1 = angleList.map { case (label, (startAngle, endAngle, color)) => {
			val p1 = getPoint(center, radius, startAngle)
			val p2 = getPoint(center, radius, endAngle)
			movePoint(center) +
				lineTo(p1) +
				arc(center, radius, startAngle, endAngle) +
				lineTo(p2) +
				fill(color) +
				fillStroke(true, false)
		}
		}.mkString("")

		//	movePoint(center.x + radius, center.y) +
		//	str1 + fillStroke(false, true)
		//circle(center,radius) +"\n"+fillStroke(false, true)
		//		rectangle(x,y,width,-height)+fillStroke(false,true)+
		roundRectangle(x, y, x + width, y - height, 5) + fillStroke(false, true) +
			str1
	}

	def main(args: Array[String]): Unit = {
		for (i <- 1 to 10) {
			randomColor()
		}
	}
}
