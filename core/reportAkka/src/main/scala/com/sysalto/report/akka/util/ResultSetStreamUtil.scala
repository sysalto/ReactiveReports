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




package com.sysalto.report.akka.util

import java.sql.ResultSet

import akka.NotUsed
import akka.stream.scaladsl.{Sink, Source}
import com.sysalto.report.util.GroupUtilDefs

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.language.postfixOps
import scala.util.control.NonFatal

trait ResultSetStreamUtil {


	implicit class ResultsetStreamToSource(rs: ResultSet) {
		def toSource: Source[Map[String, AnyRef], NotUsed] = ResultSetStream.toSource(rs)
	}

}


object ResultSetStream  {
	private def cleanup(rs: ResultSet): Unit = {
		val stmt = rs.getStatement
		try {
			if (rs != null) rs.close()
		} catch {
			case NonFatal(exception) => exception.printStackTrace()
		}
		try {
			if (stmt != null) stmt.close()
		} catch {
			case NonFatal(exception) => exception.printStackTrace()
		}
	}

	def toSource(rs: ResultSet): Source[Map[String, AnyRef], NotUsed] = {
		val stream = Source.unfoldAsync(()) { _ =>
			Future {
				if (rs.next()) Some(() -> GroupUtilDefs.toMap(rs))
				else None
			}
		}

		stream.alsoTo(Sink.onComplete {
			_ => Future(cleanup(rs))
		})
		stream
	}
}
