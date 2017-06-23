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
            initDb1();
        } catch (Exception e) {
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.close();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) {
        initDb();
    }

}
