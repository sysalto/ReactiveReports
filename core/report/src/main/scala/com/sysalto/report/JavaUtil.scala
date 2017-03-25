package com.sysalto.report

import akka.NotUsed
import akka.stream.scaladsl.Source

/**
  * Created by marian on 3/13/17.
  */
object JavaUtil {

  def getRecordValue[T](rec: Map[String, AnyRef], field: String): T = {
    import com.sysalto.report.util.ResultSetStreamUtil._
    (rec value (field)).asInstanceOf[T]
  }

  def recordToMap(rs: _root_.java.sql.ResultSet): Map[String, AnyRef] = {
    import com.sysalto.report.util.ResultSetStreamUtil._
    rs.toMap
  }

  def resultSetToSource(rs: _root_.java.sql.ResultSet): Source[Map[String, AnyRef], NotUsed] = {
    import com.sysalto.report.util.ResultSetStreamUtil._
    rs.toSource
  }

  def getRec[T](rec: Any): T = {
    assert(rec.isInstanceOf[(Option[T], Option[T], Option[T])])
    rec match {
      case (_: Option[T], crt: Option[T], _: Option[T]) =>
        crt.get
    }
  }

  def isFirstRecord[T](rec: Any): Boolean = {
    assert(rec.isInstanceOf[(Option[T], Option[T], Option[T])])
    rec match {
      case (prev: Option[T], _: Option[T], _: Option[T]) =>
        prev.isEmpty
    }
  }

  def isLastRecord[T](rec: Any): Boolean = {
    assert(rec.isInstanceOf[(Option[T], Option[T], Option[T])])
    rec match {
      case (_: Option[T], _: Option[T], next: Option[T]) =>
        next.isEmpty
    }
  }
}
