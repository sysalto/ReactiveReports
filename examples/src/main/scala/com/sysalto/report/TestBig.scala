package com.sysalto.report

import java.sql.{DriverManager, ResultSet}

import com.sysalto.render.PdfNativeFactory
import com.sysalto.report.Implicits._
import com.sysalto.report.reportTypes.{GroupUtil, ReportPageOrientation}
import com.sysalto.report.util.{GroupUtilTrait, PdfFactory}


object TestBig extends  GroupUtilTrait {
	implicit val pdfFactory: PdfFactory = new PdfNativeFactory()

	Class.forName("org.hsqldb.jdbc.JDBCDriver")
	private val conn = DriverManager.getConnection("jdbc:hsqldb:file:/home/marian/transfer/database/test", "SA", "")


	def query(sql: String): ResultSet = {
		val st = conn.createStatement()
		st.executeQuery(sql)
	}


	def report(): Unit = {

		val report = Report("big.pdf", ReportPageOrientation.PORTRAIT)
		for (i<-1 to 100000) {
				report print s"NAME${i}" at 10
				report print s"ADDRESS${i}" at 100

				if (report.lineLeft < 10) {
					report.nextPage()
				}
				report.nextLine()
			}
		report.render()
	}


	def report1(): Unit = {
		case class Food(name:String,price:Int,categ:Int)

		val list=for (i<-1 to 200 ) yield Food(s"name $i",i,i/10)
		val grp=list.iterator.toGroup

		val report = Report("report1.pdf", ReportPageOrientation.PORTRAIT)

		grp.foreach(rec=>{
			if (GroupUtil.isFirstRecord(rec)) {
				println("FIRST")
			}
			println(rec)
			if (GroupUtil.isLastRecord(rec)) {
				println("LAST")
			}
		})
//
//		for (i<-1 to 100000) {
//			report print s"NAME${i}" at 10
//			report print s"ADDRESS${i}" at 100
//
//			if (report.lineLeft < 10) {
//				report.nextPage()
//			}
//			report.nextLine()
//		}
//		report.render()
	}

	def main(args: Array[String]): Unit = {
		val t1=System.currentTimeMillis()
		try {
			report1()
		} catch {
			case e:Throwable=>e.printStackTrace()
		}
		val t2=System.currentTimeMillis()
		println((t2-t1)*0.001)
	}

}
