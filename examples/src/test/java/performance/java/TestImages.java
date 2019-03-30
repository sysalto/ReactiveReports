package performance.java;

import com.sysalto.render.PdfNativeFactory;
import com.sysalto.report.Report;
import com.sysalto.report.reportTypes.*;
import com.sysalto.report.util.PdfFactory;

public class TestImages {

    private static PdfFactory pdfFactory = new PdfNativeFactory();
    static ReportColor color = new ReportColor(220, 225, 220, 1f);
    private static String imageFolder = "examples/src/test/resources/images/";

    private static void reportTest() {

        Report report = Report.create("examples/src/test/java/performance/java/TestImages.pdf", ReportPageOrientation.LANDSCAPE(), pdfFactory, new LetterFormat(), null);
        ReportRow row = ReportRow.apply(50.f, report.pageLayout().width() - 10, Column.apply("text1").flex(4),
                Column.apply("text2").flex(1));
        ReportMargin margin1 = row.getColumnBound("text1");
        ReportMargin margin2 = row.getColumnBound("text2");
        for (int i = 1; i <= 20000; i++) {
            report.nextLine();
            ReportCell cell1 = new ReportCell(new ReportTxt("TextWWWWWW1111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111"+i)).inside(margin1).leftAlign();
            ReportCell cell2 = new ReportCell(new ReportTxt("Text2 "+i)).inside(margin2).rightAlign();
            ReportCell[] contentRow = new ReportCell[]{cell1, cell2};
            Float y2 = report.calculate(contentRow);
            String imageName = imageFolder + "icon" + (i % 10) + ".jpg";
            report.drawImage(imageName,19, report.getY(), 10f, 10f);

            report.rectangle().from(cell2.margin().left(), report.getY()).to(cell2.margin().right(),report.getY()-10).fillColor(color).draw();
            report.print(contentRow);
            report.setYPosition(y2);
            if (report.lineLeft() < 10) {
                report.nextPage();
            }
        }

        report.render();
    }

    public static void main(String[] args) {
        long t1=System.currentTimeMillis();
        reportTest();
        long t2=System.currentTimeMillis();
        System.out.println("Time:"+(t2-t1)*0.001);
    }
}
