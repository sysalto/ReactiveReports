package com.sysalto.report.reportTypes

trait ReportPageFormat {
	// values are in points 72 points = 1 inch , 1mm=2.83465 point
	val mmToPoint:Float = 2.83465f
	val width: Float
	val height: Float
}

/*

 */
class CustomFormat(widthMM: Float, heightMM: Float) extends ReportPageFormat {
	val width = widthMM * mmToPoint
	val height = heightMM * mmToPoint
}

class LetterFormat extends ReportPageFormat {
	val width = 612
	val height = 792
}

object LetterFormat extends ReportPageFormat {
	val width = 612
	val height = 792
}

object A3Format extends ReportPageFormat {
	val width = 297 * mmToPoint
	val height= 420 * mmToPoint
}

object A4Format extends ReportPageFormat {
	val width = 210 * mmToPoint
	val height = 297 * mmToPoint
}


object A5Format extends ReportPageFormat {
	val width = 148 * mmToPoint
	val height = 210 * mmToPoint
}