/*
 *  This file is part of the ReactiveReports project.
 *  Copyright (c) 2017 Sysalto Corporation.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License version 3
 * as published by the Free Software Foundation with the addition of the
 * following permission added to Section 15 as permitted in Section 7(a):
 * FOR ANY PART OF THE COVERED WORK IN WHICH THE COPYRIGHT IS OWNED BY
 * Sysalto. Sysalto DISCLAIMS THE WARRANTY OF NON INFRINGEMENT
 * OF THIRD PARTY RIGHTS.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see https://www.gnu.org/licenses/agpl-3.0.en.html.
 */

package com.sysalto.report.util

import java.sql.{Connection, ResultSet, Statement}
import java.util.logging.Logger

import akka.NotUsed
import akka.stream.scaladsl.{Sink, Source}

import scala.concurrent.{Await, Future}
import scala.language.postfixOps
import scala.util.control.NonFatal
import scala.concurrent.ExecutionContext.Implicits.global

// http://stackoverflow.com/questions/9636545/treating-an-sql-resultset-like-a-scala-stream


trait ResultSetStreamUtil {


  implicit class ResultsetToSource(rs: ResultSet) {

    def toSource:Source[Map[String,AnyRef],NotUsed] = {
      val stream = Source.unfoldAsync(()) { _ =>
        Future {
          if (rs.next()) Some(() -> toMap)
          else None
        }
      }

      stream.alsoTo(Sink.onComplete {
        _  => Future(cleanup(rs))
      })
      stream
    }

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


    def toMap:Map[String,AnyRef] = {
      val meta = rs.getMetaData
      (for (i <- 1 to meta.getColumnCount) yield {
        meta.getColumnName(i) -> rs.getObject(i)
      }).toMap
    }

  }


  implicit class GetValueRecord(rec: Map[String, AnyRef]) {
    def value(key: String):AnyRef = rec(key.toUpperCase())
  }

}


object ResultSetStreamUtil extends  ResultSetStreamUtil