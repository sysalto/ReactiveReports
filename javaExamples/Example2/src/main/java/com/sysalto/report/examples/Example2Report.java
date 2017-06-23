package com.sysalto.report.examples;

import com.sysalto.render.PdfNativeFactory;
import com.sysalto.report.Report;
import com.sysalto.report.reportTypes.*;
import com.sysalto.report.util.PdfFactory;
import com.sysalto.report.util.ResultSetGroup;
import com.sysalto.report.util.ResultSetUtil;
import scala.collection.immutable.Map;
import scala.runtime.BoxedUnit;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Example2Report {
    PdfFactory pdfFactory = new PdfNativeFactory();

    public void run(String fileName) throws Exception {

        Report report = Report.create(fileName, ReportPageOrientation.LANDSCAPE(), pdfFactory);
        report.getHeaderSize(pg -> {
            Long pgNbr = new Long(pg.toString());
            if (pgNbr == 1) return 0f;
            else return 50f;
        });
        report.getFooterSize(pg -> 30f);

        report.headerFct((pg, pgMax) -> {
            report.setYPosition(10);
            Row row = Row.apply(10.f, report.pgSize().width() - 10, Column.apply("column1").flex(1),
                    Column.apply("column2").flex(1), Column.apply("column3").flex(1));
            RMargin column1 = row.getColumnBound("column1");
            RMargin column2 = row.getColumnBound("column2");
            RMargin column3 = row.getColumnBound("column3");
            RCell h_column1 = new RCell(new RText("Type of Account").bold()).leftAllign().between(column1);
            RCell h_column2 = new RCell(new RText("Your account number").bold()).leftAllign().between(column2);
            RCell h_column3 = new RCell(new RText("Your investment statement").bold()).rightAllign().between(column3);
            RRow hrow = RRow.apply(h_column1, h_column2, h_column3);
            hrow.print(report);
            report.line().from(10, report.getY()).to(report.pgSize().width() - 10, -1).draw();
        });

        report.footerFct((pg, pgMax) -> {
            report.setYPosition(report.pgSize().height() - report.lineHeight() * 3);
            report.line().from(10, report.getY()).to(report.pgSize().width() - 10, -1).draw();
            report.nextLine();
            RCell cell = new RCell(new RText("Page " + pg + " of " + pgMax).bold()).rightAllign().between(0, report.pgSize().width() - 10);
            report.print(cell);
        });
        report.nextLine();
        ResultSet rs = Example2InitData.query("select * from clnt");
        Group group = new Group("groupName", rec -> {
            return ResultSetUtil.getRecordValue((Map<String, Object>) rec, "groupName");
        });
        GroupUtil groupUtil = GroupUtil.apply(group);

        ResultSetGroup rsGroup = ResultSetUtil.toGroup(rs);
        rsGroup.foreach(rec -> {
            Map<String, Object> crtRec = GroupUtil.getRec(rec);
            String groupName = ResultSetUtil.getRecordValue(crtRec, "groupName");
            String name = ResultSetUtil.getRecordValue(crtRec, "name");
            if (groupUtil.isHeader("groupName", rec)) {
                report.print(new RCell(new RText("HEADER " + groupName).bold()).at(50));
                report.nextLine();
            }
            report.print(new RCell(new RText(name)).at(50));
            report.nextLine();
            if (groupUtil.isFooter("groupName", rec)) {
                report.print(new RCell(new RText("FOOTER " + groupName).bold()).at(50));
                report.nextLine();
            }
            if (report.lineLeft() < 10) {
                report.newPage();
            }
            return BoxedUnit.UNIT;
        });

        rs.close();
        report.render();
        report.close();

    }

    public static void main(String[] args) throws Exception {
        Example2InitData.init();
        new Example2Report().run("Example2.pdf");
    }
}
