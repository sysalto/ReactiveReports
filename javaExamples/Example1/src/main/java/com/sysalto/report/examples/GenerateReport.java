package com.sysalto.report.examples;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

public class GenerateReport extends HttpServlet {


    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        MutualFundsInitData.initDb();
        try {
            String reportName = File.createTempFile("Report", ".pdf").getAbsolutePath();
            new MutualFundsNoAkkaJavaReport().run(reportName);
            performTask(req, resp, reportName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void performTask(HttpServletRequest request, HttpServletResponse response, String reportName) throws ServletException,
            IOException {

        String pdfFileName = reportName.substring(reportName.lastIndexOf("/") + 1);
        File pdfFile = new File(reportName);

        response.setContentType("application/pdf");
        response.addHeader("Content-Disposition", "attachment; filename=" + pdfFileName);
        response.setContentLength((int) pdfFile.length());

        FileInputStream fileInputStream = new FileInputStream(pdfFile);
        OutputStream responseOutputStream = response.getOutputStream();
        int bytes;
        while ((bytes = fileInputStream.read()) != -1) {
            responseOutputStream.write(bytes);
        }

    }
}
