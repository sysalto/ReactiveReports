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




package com.sysalto.report.examples.mutualFunds

import java.sql.{DriverManager, ResultSet}


object MutualFundsInitData {
  Class.forName("org.hsqldb.jdbc.JDBCDriver")
  private val conn = DriverManager.getConnection("jdbc:hsqldb:mem:mymemdb", "SA", "")
//  private val conn = DriverManager.getConnection("jdbc:hsqldb:file:/home/marian/transfer/database/test", "SA", "")

  private def dbUpdate(sql: String): Unit = {
    val st = conn.createStatement()
    st.executeUpdate(sql)
    st.close()
  }

  def query(sql: String): ResultSet = {
    val st = conn.createStatement()
    st.executeQuery(sql)
  }



  def initDb1(): Unit = {
    dbUpdate(
      """drop table test if exists""")
    dbUpdate(
      """create table test (
    |   name varchar(255),
    |   address varchar(255))""".stripMargin)
    for (i<-1 to 10000000) {
      try {
        dbUpdate(s"insert into test values('name${i}','address${i}')")
      } catch {
        case e: Throwable =>
          e.printStackTrace()
      }
    }
  }

  def initDb(): Unit = {
    dbUpdate(
      """create table clnt (
        |   name varchar(255),
        |		addr1 varchar(255),
        |		addr2 varchar(255),
        |		addr3 varchar(255),
        |		accountNbr integer,
        |		branch_addr1 varchar(255),
        |		branch_addr2 varchar(255),
        |		branch_addr3 varchar(255),
        |		benef_name varchar(255)
        |  )""".stripMargin)

    dbUpdate(
      """insert into  clnt (
        |     name,addr1,addr2,addr3,accountNbr,branch_addr1,branch_addr2,branch_addr3,benef_name
        |     ) values (
        |         'John Mill',' 1 Main','New York,NY','200100',123,'10 Main','New York,NY','22111','Mary Mill'
        |       )
        |  """.stripMargin)

    //sum investment table
    dbUpdate(
      """create table sum_investment (
        |   fund_name varchar(255),
        |		value1 numeric,
        |		value2 numeric
        |  )""".stripMargin)

    dbUpdate(
      """insert into  sum_investment (
        |     fund_name,value1,value2
        |     ) values (
        |         'Money Market Fund',1000.0,1200.0
        |       )
        |  """.stripMargin)

    dbUpdate(
      """insert into  sum_investment (
        |     fund_name,value1,value2
        |     ) values (
        |         'Fixed Income Funds',18544.44,18826.21
        |       )
        |  """.stripMargin)

    dbUpdate(
      """insert into  sum_investment (
        |     fund_name,value1,value2
        |     ) values (
        |         'Balanced Funds',12345.65,12423.0
        |       )
        |  """.stripMargin)

    dbUpdate(
      """insert into  sum_investment (
        |     fund_name,value1,value2
        |     ) values (
        |         'Equity Funds',2340,2500
        |       )
        |  """.stripMargin)

    dbUpdate(
      """insert into  sum_investment (
        |     fund_name,value1,value2
        |     ) values (
        |         'US Funds',2000,2050
        |       )
        |  """.stripMargin)

    dbUpdate(
      """insert into  sum_investment (
        |     fund_name,value1,value2
        |     ) values (
        |         'International Equity Funds',3200,3250
        |       )
        |  """.stripMargin)

    dbUpdate(
      """insert into  sum_investment (
        |     fund_name,value1,value2
        |     ) values (
        |         'Global Equity Funds',4000,4025
        |       )
        |  """.stripMargin)

    dbUpdate(
      """create table tran_account (
        |   name varchar(255),
        |		value1 numeric,
        |		value2 numeric,
        |		value3 numeric
        |  )""".stripMargin)

    dbUpdate(
      """insert into  tran_account (
        |     name,value1,value2,value3
        |     ) values (
        |         'Begining account value',12300.0,13300.0,13300.0
        |       )
        |  """.stripMargin)

    dbUpdate(
      """insert into  tran_account (
        |     name,value1,value2,value3
        |     ) values (
        |         'Amount in',780,900,900
        |       )
        |  """.stripMargin)

    dbUpdate(
      """insert into  tran_account (
        |     name,value1,value2,value3
        |     ) values (
        |         'Amount out',0,0,0
        |       )
        |  """.stripMargin)

    dbUpdate(
      """insert into  tran_account (
        |     name,value1,value2,value3
        |     ) values (
        |         'Change in the value of your account',500,700,700
        |       )
        |  """.stripMargin)

    dbUpdate(
      """create table account_perf (
        |   value3m varchar(255),
        |		value1y varchar(255),
        |		value3y varchar(255),
        |		value5y varchar(255),
        |		value10y varchar(255),
        |		annualized varchar(255)
        |  )""".stripMargin)

    dbUpdate(
      """insert into  account_perf (
        |     value3m,value1y,value3y,value5y,value10y,annualized
        |     ) values (
        |         '2.22','N/A','N/A','N/A','N/A','5'
        |       )
        |  """.stripMargin)


  }
}
