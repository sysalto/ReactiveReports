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

import java.util.Date

import scala.collection.immutable.ListMap


object SqlUtil {

  object DbTypes extends Enumeration {
    val Integer, Varchar, Date, Numeric = Value
  }

  def generateTable(tableName: String, content: List[ListMap[String, Any]]): String = {
    val fieldType = content.head.map {
      case (key, value) => value match {
        case _: Integer => (key, DbTypes.Integer)
        case _: String => (key, DbTypes.Varchar)
        case _: Date => (key, DbTypes.Date)
        case _: Double => (key, DbTypes.Numeric)
      }
    }
    val fieldList = fieldType.keySet.toList
    val fieldDef = fieldList.map(field => {
      val vtype = fieldType(field) match {
        case DbTypes.Integer => "integer"
        case DbTypes.Varchar => "varchar(255)"
        case DbTypes.Date => "date"
        case DbTypes.Numeric => "numeric"
      }
      field + " " + vtype
    })
    val fieldsDef = fieldDef.mkString(",\n||\t\t")
    val createSql =
      s"""dbUpdate(
         |   \"\"\"create table $tableName (
         ||   $fieldsDef
         ||  )\"\"\".stripMargin)
         |""".stripMargin

    val insertList = content.map(row => {
      val valueList = fieldList.map(field => {
        val fieldVal = row(field)
        fieldType(field) match {
          case DbTypes.Integer => fieldVal
          case DbTypes.Varchar => s"""'$fieldVal'"""
          case DbTypes.Date => fieldVal
          case DbTypes.Numeric => fieldVal
        }
      })
      s"""dbUpdate(
         |  \"\"\"insert into  $tableName (
         ||     ${fieldList.mkString(",")}
         ||     ) values (
         ||         ${valueList.mkString(",")}
         ||       )
         ||  \"\"\".stripMargin)
       """.stripMargin
    })

    createSql + "\n" + insertList.mkString("\n")
  }


}
