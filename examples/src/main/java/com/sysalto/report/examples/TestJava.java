package com.sysalto.report.examples;

import com.sysalto.render.PdfNativeFactory;
import com.sysalto.report.Report;
import com.sysalto.report.reportTypes.LetterFormat;
import com.sysalto.report.reportTypes.ReportCell;
import com.sysalto.report.reportTypes.ReportPageOrientation;
import com.sysalto.report.reportTypes.ReportTxt;
import com.sysalto.report.util.PdfFactory;

public class TestJava {
    private static PdfFactory pdfFactory = new PdfNativeFactory();

    public static void run() {
        Report report = Report.create("TestJava.pdf", ReportPageOrientation.LANDSCAPE(), pdfFactory, new LetterFormat(), null);
        report.start();
        report.nextLine(5);
//        final ReportCell cel11 = new ReportCell(new ReportTxt("a").size(15).bold());
//        report.print(cel11);
//        report.nextLine();

        ReportCell[] row = new ReportCell[]{new ReportCell(new ReportTxt("some string")).inside(10,50), new ReportCell(new ReportTxt("some string")).inside(60,100)};
        Float y = report.calculate(row);
        System.out.println("Y:"+y);
//        report.print(row);
//        report.nextLine(2);
        report.render();
    }

    public static void main(String[] args) throws Exception {
        run();
    }
}
