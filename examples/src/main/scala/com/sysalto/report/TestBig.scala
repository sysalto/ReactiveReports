package com.sysalto.report

import java.sql.{DriverManager, ResultSet}

import com.sysalto.render.PdfNativeFactory
import com.sysalto.report.Implicits._
import com.sysalto.report.reportTypes.{GroupUtil, ReportPageOrientation}
import com.sysalto.report.util.{PdfFactory, ResultSetUtilTrait}


object TestBig extends ResultSetUtilTrait {
	implicit val pdfFactory: PdfFactory = new PdfNativeFactory()

	Class.forName("org.hsqldb.jdbc.JDBCDriver")
	private val conn = DriverManager.getConnection("jdbc:hsqldb:file:/home/marian/transfer/database/test", "SA", "")


	def query(sql: String): ResultSet = {
		val st = conn.createStatement()
		st.executeQuery(sql)
	}


	def report(): Unit = {

		val report = Report("big.pdf", ReportPageOrientation.PORTRAIT)
		for (i<-1 to 10000000) {
				report print s"NAME${i}" at 10
				report print s"ADDRESS${i}" at 100

				if (report.lineLeft < 10) {
					report.nextPage()
				}
				report.nextLine()
			}
		report.render()
	}

	def main(args: Array[String]): Unit = {
		val t1=System.currentTimeMillis()
		try {
			report()
		} catch {
			case e:Throwable=>e.printStackTrace()
		}
		val t2=System.currentTimeMillis()
		println((t2-t1)*0.001)
	}

}
