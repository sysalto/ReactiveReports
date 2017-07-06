/*
 * ReactiveReports - Free Java /Scala Reporting Library.
 * Copyright (C) 2017 SysAlto Corporation. All rights reserved.
  *
 * Unless you have purchased a commercial license agreement from SysAlto
 * the following license terms apply:
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



package com.sysalto.report.examples;

import java.io.File;
import java.sql.*;

public class Example2InitData {
    static Connection conn = null;

    static private void dbUpdate(String sql) throws SQLException {
        Statement st = conn.createStatement();
        st.executeUpdate(sql);
        st.close();
    }

    static public ResultSet query(String sql) throws SQLException {
        Statement st = conn.createStatement();
        return st.executeQuery(sql);
    }

    static public void init() throws Exception {
        Class.forName("org.hsqldb.jdbc.JDBCDriver");
        String tmpName = File.createTempFile("database", "dat").getAbsolutePath();
        String path=tmpName.substring(0,tmpName.lastIndexOf("/"));
        conn = DriverManager.getConnection("jdbc:hsqldb:file:"+path+"/example2", "SA", "");
    }

    static private void initDb1() throws SQLException {
        dbUpdate("DROP table IF EXISTS clnt");
        dbUpdate("create table clnt ( " +
                "groupName varchar(255)," +
                "name varchar(255)," +
                "addr varchar(255))");
        Long groupNbr = 1L;
        for (int i = 1; i < 3000; i++) {
            if (i % 30 == 0) {
                groupNbr++;
            }
            String group = "Group" + groupNbr;
            dbUpdate("insert into  clnt ( " +
                    "groupName,name, addr) " +
                    "values('"+group+"','Name" + i + "',' " + i + " Main New York,NY')");
        }

    }

    static public void initDb() {
        try {
            init();
            query("select count(*) from clnt");

        } catch (Exception e) {
            try {
                initDb1();
            } catch (SQLException e1) {
                e1.printStackTrace();
                if (conn != null) {
                    try {
                        conn.close();
                    } catch (Exception e2) {
                        e2.printStackTrace();
                    }
                }
            }
        }
    }

    public static void main(String[] args) {
        initDb();
    }

}
