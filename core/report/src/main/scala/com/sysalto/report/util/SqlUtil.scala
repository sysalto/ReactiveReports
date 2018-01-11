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
