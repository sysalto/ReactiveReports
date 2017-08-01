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

public class Example1Report {
    static RColor headerColor = new RColor(156, 76, 6, 1f);
    static RColor headerFontColor = new RColor(255, 255, 255, 1f);

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
            RCell cell = new RCell(new RText("Header").bold()).rightAllign().between(0, report.pgSize().width() - 10);
            report.print(cell);
        });

        report.footerFct((pg, pgMax) -> {
            report.setYPosition(report.pgSize().height() - report.lineHeight() * 3);
            report.line().from(10, report.getY()).to(report.pgSize().width() - 10, -1).draw();
            report.nextLine();
            RCell cell = new RCell(new RText("Page " + pg + " of " + pgMax).bold()).rightAllign().between(0, report.pgSize().width() - 10);
            report.print(cell);
        });
        report.nextLine();
        ResultSet rs = Example1InitData.query("select * from clnt");
        Group group = new Group("groupName", rec -> {
            return ResultSetUtil.getRecordValue((Map<String, Object>) rec, "groupName");
        });
        GroupUtil groupUtil = GroupUtil.apply(group);
        Row row = Row.apply(10.f, report.pgSize().width() - 10, Column.apply("name", 150f),
                Column.apply("addr", 150f));
        RMargin m_name = row.getColumnBound("name");
        RMargin m_addr = row.getColumnBound("addr");
        RCell h_name = new RCell(new RText("Name").bold().color(headerFontColor)).leftAllign().between(m_name);
        RCell h_addr = new RCell(new RText("Addr").bold().color(headerFontColor)).leftAllign().between(m_addr);

        ResultSetGroup rsGroup = ResultSetUtil.toGroup(rs);
        rsGroup.foreachJ(rec -> {
            Map<String, Object> crtRec = GroupUtil.getRec(rec);
            String groupName = ResultSetUtil.getRecordValue(crtRec, "groupName");
            String name = ResultSetUtil.getRecordValue(crtRec, "name");
            String addr = ResultSetUtil.getRecordValue(crtRec, "addr");
            if (groupUtil.isHeader("groupName", rec)) {
                if (report.lineLeft() < 35) {
                    report.newPage();
                }
                report.print(new RCell(new RText("HEADER " + groupName).bold()).at(50));
                report.nextLine(2);
                RRow hrow = RRow.apply(h_name, h_addr);
                Float y2 = hrow.calculate(report);
                report.rectangle().from(9, report.getY() - report.lineHeight()).radius(3).to(report.pgSize().width() - 9, y2 + 2).fillColor(headerColor).draw();
                hrow.print(report);
                report.setYPosition(y2);
                report.nextLine();
            }
            RCell d_name = new RCell(new RText(name)).leftAllign().between(m_name);
            RCell d_addr = new RCell(new RText(addr)).leftAllign().between(m_addr);
            RRow drow = RRow.apply(d_name, d_addr);
            drow.print(report);
            report.nextLine();
            if (groupUtil.isFooter("groupName", rec)) {
                report.print(new RCell(new RText("FOOTER " + groupName).bold()).at(50));
                report.nextLine();
            }
            if (report.lineLeft() < 3) {
                report.newPage();
            }
        });

        rs.close();
        report.render();
        report.close();

    }

    public static void main(String[] args) throws Exception {
        Example1InitData.initDb();
        new Example1Report().run("Example1.pdf");
    }
}
