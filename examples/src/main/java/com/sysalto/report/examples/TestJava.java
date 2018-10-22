package com.sysalto.report.examples;

import com.sysalto.render.PdfNativeFactory;
import com.sysalto.report.Report;
import com.sysalto.report.ReportChart;
import com.sysalto.report.reportTypes.*;
import com.sysalto.report.util.PdfFactory;

import java.util.List;

public class TestJava {
    private static PdfFactory pdfFactory = new PdfNativeFactory();

    public static void run() {
        Report report = Report.create("TestJava.pdf", ReportPageOrientation.LANDSCAPE(), pdfFactory, new LetterFormat(), null);
        report.start();
        report.nextLine(2);

        ReportChart reportChart = new ReportChart(report);
        List chartData=new java.util.ArrayList<scala.Tuple3<String,ReportColor, Object>>();
        chartData.add(new scala.Tuple3("Item1", new ReportColor(60, 100, 200,1), 70.53f));
        chartData.add(new scala.Tuple3("B", new ReportColor(100, 255, 200,1), 30f));
        chartData.add(new scala.Tuple3("C", new ReportColor(200, 10, 200,1), 40f));
        chartData.add(new scala.Tuple3("D", new ReportColor(10, 200, 200,1), 90f));



        reportChart.barChart("Test", chartData,300,400,200,100,5);

        report.render();
    }

    public static void main(String[] args) throws Exception {
        run();
    }
}
