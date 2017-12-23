package com.sysalto.report

import java.sql.{DriverManager, ResultSet}


import com.sysalto.render.PdfNativeFactory
import com.sysalto.report.Implicits._
import com.sysalto.report.reportTypes.{GroupUtil, ReportPageOrientation}
import com.sysalto.report.util.{PdfFactory, ResultSetUtilTrait}


object TestBirt extends ResultSetUtilTrait {
	implicit val pdfFactory: PdfFactory = new PdfNativeFactory()

	Class.forName("org.hsqldb.jdbc.JDBCDriver")
	private val conn = DriverManager.getConnection("jdbc:hsqldb:file:/home/marian/transfer/database/test", "SA", "")


	def query(sql: String): ResultSet = {
		val st = conn.createStatement()
		st.executeQuery(sql)
	}


	def report(): Unit = {

		val report = Report("birt.pdf", ReportPageOrientation.PORTRAIT)
		val rs = query("select  * from test")
		val rsGroup = rs.toGroup
		rsGroup.foreach(
			rec => try {
				val crtRec = GroupUtil.getRec(rec)
				report print (crtRec("NAME").toString) at 10
				report print (crtRec("ADDRESS").toString) at 100

				if (report.lineLeft < 10) {
					report.newPage()
				}
				report.nextLine()
			} catch {
				case e: Throwable =>
					e.printStackTrace()
			})
		rs.close()
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
