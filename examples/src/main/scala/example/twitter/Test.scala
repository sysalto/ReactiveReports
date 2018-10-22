package example.twitter

import com.sysalto.report.Implicits._
import com.sysalto.render.PdfDraw.DrawPoint
import com.sysalto.render.PdfNativeFactory
import com.sysalto.render.basic.PdfBasic
import com.sysalto.report.{DirectDrawReport, ReportChart}
import com.sysalto.report.reportTypes.{ReportColor, ReportCell => _, ReportMargin => _, _}

object Test {

	def report(): Unit = {
		implicit val pdfFactory = new PdfNativeFactory()
		val report = Report("test.pdf", ReportPageOrientation.LANDSCAPE, LetterFormat, null, false)
		report.nextLine(2)

		val reportChart = new ReportChart(report)
		val chartData = List(("Item1", ReportColor(60, 100, 200), 70.53f), ("B", ReportColor(100, 255, 200), 30.0f),
			("C", ReportColor(200, 10, 200), 40.0f),("D", ReportColor(10, 200, 200), 90.0f))
		reportChart.pieChart(report.font, "", chartData, 15, 10, 200, 150)

		reportChart.barChart("Test",chartData,300,400,200,100,5)

		report.render()
	}

	def main(args: Array[String]): Unit = {
		report()

	}

}
