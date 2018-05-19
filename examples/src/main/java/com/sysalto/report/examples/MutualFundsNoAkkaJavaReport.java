package com.sysalto.report.examples;

import com.sysalto.render.PdfNativeFactory;
import com.sysalto.report.Report;
import com.sysalto.report.ReportTypes;
import com.sysalto.report.examples.mutualFunds.MutualFundsInitData;
import com.sysalto.report.reportTypes.*;
import com.sysalto.report.util.*;
import org.apache.derby.iapi.util.ByteArray;
import scala.collection.immutable.Map;

import java.io.File;
import java.math.BigDecimal;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;

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


public class MutualFundsNoAkkaJavaReport {
    static private Date date1 = (new GregorianCalendar(2013, 0, 1)).getTime();
    static private Date date2 = (new GregorianCalendar(2013, 11, 31)).getTime();
    static ReportColor headerColor = new ReportColor(156, 76, 6, 1f);
    static ReportColor headerFontColor = new ReportColor(255, 255, 255, 1f);

    static private final SimpleDateFormat sd = new SimpleDateFormat("MMM dd yyyy");

    PdfFactory pdfFactory = new PdfNativeFactory();

    private void run() throws Exception {


        final PersistenceFactory persistenceDerbyFactory = new PersistenceFactory() {
            @Override
            public PersistenceUtil getPersistence() {
                return new DerbyPersistenceUtil();
            }
        };


        final PersistenceFactory persistenceMemoryFactory = new PersistenceFactory() {
            @Override
            public PersistenceUtil getPersistence() {
                return new MemoryPersistenceUtil();
            }
        };


//        Report report = Report.create("MutualFundsJava.pdf", ReportPageOrientation.LANDSCAPE(), pdfFactory);
        Report report = Report.create("MutualFundsJava.pdf", ReportPageOrientation.LANDSCAPE(), pdfFactory,new LetterFormat(), persistenceMemoryFactory);

        report.newPageFctCallback(pg -> {
            drawbackgroundImage(report);
        });

        report.headerSizeCallback(pg -> {
            if (pg == 1) return 0f;
            else return 50f;
        });
        report.footerSizeCallback(pg -> 30f);

        report.headerFct((pg, pgMax) -> {
            report.setYPosition(10);
            ReportRow reportRow = ReportRow.apply(10.f, report.pageLayout().width() - 10, Column.apply("column1").flex(1),
                    Column.apply("column2").flex(1), Column.apply("column3").flex(1));
            ReportMargin column1 = reportRow.getColumnBound("column1");
            ReportMargin column2 = reportRow.getColumnBound("column2");
            ReportMargin column3 = reportRow.getColumnBound("column3");
            ReportCell h_column1 = new ReportCell(new ReportTxt("Type of Account").bold()).leftAlign().inside(column1);
            ReportCell h_column2 = new ReportCell(new ReportTxt("Your account number").bold()).leftAlign().inside(column2);
            ReportCell h_column3 = new ReportCell(new ReportTxt("Your investment statement").bold()).rightAlign().inside(column3);
            ReportCell[] hrow = new ReportCell[]{h_column1, h_column2, h_column3};
            report.print(hrow);
            report.nextLine();
            String str = sd.format(date1) + " to " + sd.format(date2);
            ReportCell r_column1 = new ReportCell(new ReportTxt("Group Registered Retirement Saving Plan")).leftAlign().inside(column1);
            ReportCell r_column2 = new ReportCell(new ReportTxt("123456789")).leftAlign().inside(column2);
            ReportCell r_column3 = new ReportCell(new ReportTxt(str)).rightAlign().inside(column3);

            ReportCell[] rrow = new ReportCell[]{r_column1, r_column2, r_column3};
            report.print(rrow);
            report.nextLine(2);
            report.line().from(10, report.getY()).to(report.pageLayout().width() - 10, -1).draw();
        });

        report.footerFct((pg, pgMax) -> {
            report.setYPosition(report.pageLayout().height() - report.lineHeight() * 3);
            report.line().from(10, report.getY()).to(report.pageLayout().width() - 10, -1).draw();
            report.nextLine();
            ReportCell cell = new ReportCell(new ReportTxt("Page " + pg + " of " + pgMax).bold()).rightAlign().inside(0, report.pageLayout().width() - 10);
            report.print(cell);
        });
        report.start();
        reportHeader(report);
        summaryOfInvestment(report);
        changeAccount(report);
        accountPerformance(report);
        disclaimer(report);
        report.render();
    }


    private void reportHeader(Report report) throws Exception {

        ResultSet rs = MutualFundsInitData.query("select * from clnt");
        rs.next();

        Map<String, Object> record = GroupUtilDefs.toMap(rs);
        rs.close();
        report.nextLine();
        report.drawImage("examples/src/main/resources/images/bank_banner.jpg", 5f, 45f, 100f, 40f);
        ReportMargin margin = new ReportMargin(0, report.pageLayout().width() - 10);
        report.print(new ReportCell(new ReportTxt("Investment statement").size(15).bold()).rightAlign().inside(margin));
        report.nextLine();
        String str = sd.format(date1) + " to " + sd.format(date2);
        report.print(new ReportCell(new ReportTxt(str).size(15).bold()).rightAlign().inside(margin));
        report.nextLine(2);
        report.print(new ReportCell(new ReportTxt("Mutual Funds Inc.").bold()).at(10));
        report.nextLine();
        report.print(new ReportCell(new ReportTxt("Group Registered Retirement Saving Plan").bold()).at(10));
        report.nextLine(2);
        Float y = report.getY();
        report.print(new ReportCell(new ReportTxt(GroupUtilDefs.getRecordValue(record, "name").toString()).bold()).at(10));
        report.nextLine();
        report.print(new ReportCell(new ReportTxt(GroupUtilDefs.getRecordValue(record, "addr1").toString())).at(10));
        report.nextLine();
        report.print(new ReportCell(new ReportTxt(GroupUtilDefs.getRecordValue(record, "addr2").toString())).at(10));
        report.nextLine();
        report.print(new ReportCell(new ReportTxt(GroupUtilDefs.getRecordValue(record, "addr3").toString())).at(10));
        report.setYPosition(y);
        report.print(new ReportCell(new ReportTxt("Beneficiary information").bold()).at(500));
        report.nextLine();
        report.print(new ReportCell(new ReportTxt(GroupUtilDefs.getRecordValue(record, "benef_name").toString())).at(500));
        report.nextLine(2);
    }


    private void summaryOfInvestment(Report report) throws Exception {

        report.nextLine(2);
        ReportRow reportRow = ReportRow.apply(10.f, report.pageLayout().width() - 10, Column.apply("fund_name", 150f),
                Column.apply("value1").flex(1), Column.apply("value2").flex(1), Column.apply("change").flex(1),
                Column.apply("graphic").flex(2));
        ReportMargin m_fundName = reportRow.getColumnBound("fund_name");
        ReportMargin m_value1 = reportRow.getColumnBound("value1");
        ReportMargin m_value2 = reportRow.getColumnBound("value2");
        ReportMargin m_change = reportRow.getColumnBound("change");
        ReportMargin m_graphic = reportRow.getColumnBound("graphic");
        ReportCell c_fundName = new ReportCell(new ReportTxt("Summary of investments").bold().color(headerFontColor)).leftAlign().
                inside(m_fundName);
        ReportCell c_value1 = new ReportCell(new ReportTxt("Value on\n" + sd.format(date1) + "($$)").bold().color(headerFontColor)).rightAlign().
                inside(m_value1);
        ReportCell c_value2 = new ReportCell(new ReportTxt("Value on\n" + sd.format(date2) + "($$)").bold().color(headerFontColor)).rightAlign().
                inside(m_value2);
        ReportCell c_change = new ReportCell(new ReportTxt("Change($$)").bold().color(headerFontColor)).rightAlign().inside(m_change);
        ReportCell c_graphic = new ReportCell(new ReportTxt("Assets mix\n" + sd.format(date2) + "(%)").bold().color(headerFontColor)).rightAlign().inside(m_graphic);
        ReportCell[] rrow = new ReportCell[]{c_fundName, c_value1, c_value2, c_change, c_graphic};
        Float y2 = report.calculate(rrow);
        Float top = report.getY() - report.lineHeight();
        Float bottom = y2 + 2;
        report.rectangle().from(9, top).radius(3).to(report.pageLayout().width() - 9, bottom).fillColor(headerColor).draw();


        report.print(CellAlign.CENTER, top, bottom, rrow);
        report.setYPosition(y2);
        report.nextLine();
        ResultSet rs = MutualFundsInitData.query("select * from sum_investment");
        ResultSetGroup rsGroup = GroupUtilDefs.toGroup(rs);
        AtomicReference<Float> firstY = new AtomicReference<>();
        AtomicReference<Double> total1 = new AtomicReference<>();
        AtomicReference<Double> total2 = new AtomicReference<>();
        AtomicReference<Double> total3 = new AtomicReference<>();
        final AtomicReference<Integer> firstChar = new AtomicReference<>();
        AtomicReference<List<scala.Tuple2<String, Object>>> chartData = new AtomicReference<>();
        chartData.set(new java.util.ArrayList<scala.Tuple2<String, Object>>());
        total1.set(0.);
        total2.set(0.);
        total3.set(0.);
        firstChar.set((int) 'A');
        rsGroup.foreachJ(rec -> {
            if (GroupUtil.isFirstRecord(rec)) {
                firstY.set(report.getY());
            }
            char cc = (char) (firstChar.get().intValue());
            Map<String, Object> crtRec = GroupUtil.getRec(rec);
            String fund_name = GroupUtilDefs.getRecordValue(crtRec, "fund_name");
            BigDecimal value1 = GroupUtilDefs.getRecordValue(crtRec, "value1");
            BigDecimal value2 = GroupUtilDefs.getRecordValue(crtRec, "value2");
            RTextList fundTxt = new ReportTxt(cc + " ").bold().plus(new ReportTxt(fund_name));
            ReportCell cr_fundName = ReportCell.apply(fundTxt).leftAlign().inside(m_fundName);
            ReportCell cr_value1 = new ReportCell(new ReportTxt(value1.toString())).rightAlign().inside(m_value1);
            ReportCell cr_value2 = new ReportCell(new ReportTxt(value2.toString())).rightAlign().inside(m_value2);
            Float v_change = value2.floatValue() - value1.floatValue();
            total1.set(total1.get() + value1.floatValue());
            total2.set(total2.get() + value2.floatValue());
            total3.set(total3.get() + v_change.floatValue());

            chartData.get().add(new scala.Tuple2("" + cc, total2.get()));
            ReportCell cr_change = new ReportCell(new ReportTxt(v_change.toString())).rightAlign().inside(m_change);
            ReportCell[] rrow1 = new ReportCell[]{cr_fundName, cr_value1, cr_value2, cr_change};
            Float y3 = report.calculate(rrow1);
            report.print(rrow1);

            if (GroupUtil.isLastRecord(rec)) {
                report.line().from(10, report.getY() + 2).to(m_change.right(), -1).width(0.1f).draw();
            } else {
                report.line().from(10, report.getY() + 2).to(m_change.right(), -1).color(200, 200, 200).lineType(new LineDashType(2, 1)).width(0.5f).draw();
            }
            firstChar.set(firstChar.get() + 1);
            report.nextLine();
        });


        ReportCell[] trow = new ReportCell[]{new ReportCell(new ReportTxt("Total").bold()).inside(m_fundName),
                new ReportCell(new ReportTxt(total1.toString()).bold()).rightAlign().inside(m_value1),
                new ReportCell(new ReportTxt(total2.toString()).bold()).rightAlign().inside(m_value2),
                new ReportCell(new ReportTxt(total3.toString()).bold()).rightAlign().inside(m_change)};
        report.print(trow);
        float chartHeight = report.getY() - firstY.get() - 10;
        report.drawPieChart1("", chartData.get(), m_graphic.left() + 5, firstY.get() - report.lineHeight() + 5, m_graphic.right() -
                m_graphic.left() - 10, chartHeight);
    }

    private void drawbackgroundImage(Report report) {
        report.rectangle().from(0, 0).to(report.pageLayout().width(), report.pageLayout().height()).
                verticalShade(new ReportColor(255, 255, 255, 1), new ReportColor(255, 255, 180, 1)).draw();
    }


    private void changeAccount(Report report) throws Exception {
        report.nextLine(2);
        ReportRow reportRow = ReportRow.apply(10.f, report.pageLayout().width() - 10, Column.apply("account", 250f),
                Column.apply("value1").flex(1), Column.apply("value2").flex(1), Column.apply("value3").flex(1));
        ReportMargin account = reportRow.getColumnBound("account");
        ReportMargin value1 = reportRow.getColumnBound("value1");
        ReportMargin value2 = reportRow.getColumnBound("value2");
        ReportMargin value3 = reportRow.getColumnBound("value3");
        ReportCell accountHdr = new ReportCell(new ReportTxt("Change in the value of account").bold().
                color(headerFontColor)).leftAlign().inside(account);
        ReportCell value1Hdr = new ReportCell(new ReportTxt("This period($)").bold().
                color(headerFontColor)).rightAlign().inside(value1);
        ReportCell value2Hdr = new ReportCell(new ReportTxt("Year-to-date($)").bold().
                color(headerFontColor)).rightAlign().inside(value2);
        ReportCell value3Hdr = new ReportCell(new ReportTxt("Since\n" + sd.format(date1) + "($)").bold().
                color(headerFontColor)).rightAlign().inside(value3);
        ReportCell[] rrow = new ReportCell[]{accountHdr, value1Hdr, value2Hdr, value3Hdr};
        float y2 = report.calculate(rrow);
        Float top = report.getY() - report.lineHeight();
        Float bottom = y2 + 2;
        report.rectangle().from(9, top).radius(3).to(report.pageLayout().width() - 9, bottom).fillColor(headerColor).draw();
        report.print(CellAlign.CENTER, top, bottom, rrow);
        report.setYPosition(y2);
        report.nextLine();
        ResultSet rs = MutualFundsInitData.query("select * from tran_account");
        AtomicReference<Double> total1 = new AtomicReference<>();
        AtomicReference<Double> total2 = new AtomicReference<>();
        AtomicReference<Double> total3 = new AtomicReference<>();
        total1.set(0.);
        total2.set(0.);
        total3.set(0.);

        ResultSetGroup rsGroup = GroupUtilDefs.toGroup(rs);
        rsGroup.foreachJ(rec -> {
            Map<String, Object> crtRec = GroupUtil.<scala.collection.immutable.Map<java.lang.String, java.lang.Object>>getRec(rec);
            String name = GroupUtilDefs.getRecordValue(crtRec, "name");
            BigDecimal r_value1 = GroupUtilDefs.getRecordValue(crtRec, "value1");
            BigDecimal r_value2 = GroupUtilDefs.getRecordValue(crtRec, "value2");
            BigDecimal r_value3 = GroupUtilDefs.getRecordValue(crtRec, "value3");
            ReportCell c_account = new ReportCell(new ReportTxt(name)).leftAlign().inside(account);
            ReportCell c_value1 = new ReportCell(new ReportTxt(r_value1.toString())).rightAlign().inside(value1);
            ReportCell c_value2 = new ReportCell(new ReportTxt(r_value2.toString())).rightAlign().inside(value2);
            ReportCell c_value3 = new ReportCell(new ReportTxt(r_value3.toString())).rightAlign().inside(value3);
            total1.set(total1.get() + r_value1.doubleValue());
            total2.set(total2.get() + r_value2.doubleValue());
            total3.set(total3.get() + r_value3.doubleValue());
            ReportCell[] rrow1 = new ReportCell[]{c_account, c_value1, c_value2, c_value3};
            Float y21 = report.calculate(rrow1);
            report.print(rrow1);
            ReportColor rcolor = null;
            if (GroupUtil.isLastRecord(rec)) {
                rcolor = new ReportColor(0, 0, 0, 1f);
            } else {
                rcolor = new ReportColor(200, 200, 200, 1f);
            }
            report.line().from(10, report.getY() + 2).to(value3.right(), -1).color(rcolor).width(0.5f).draw();
            report.nextLine();
        });

        rs.close();
        ReportCell accountSum = new ReportCell(new ReportTxt("Value of  account on " + sd.format(date2)).bold()).leftAlign().inside(account);
        ReportCell value1Sum = new ReportCell(new ReportTxt("" + total1.get()).bold()).rightAlign().inside(value1);
        ReportCell value2Sum = new ReportCell(new ReportTxt("" + total2.get()).bold()).rightAlign().inside(value2);
        ReportCell value3Sum = new ReportCell(new ReportTxt("" + total3.get()).bold()).rightAlign().inside(value3);
        ReportCell[] frow = new ReportCell[]{accountSum, value1Sum, value2Sum, value3Sum};
        Float y3 = report.calculate(frow);
        report.print(frow);
        report.setYPosition(y3);
        report.nextLine(3);
    }

    private void accountPerformance(Report report) throws Exception {
        ResultSet rs = MutualFundsInitData.query("select * from account_perf");
        rs.next();
        Map<String, Object> record = GroupUtilDefs.toMap(rs);
        rs.close();
        ReportRow reportRow = ReportRow.apply(10.f, report.pageLayout().width() - 10, Column.apply("account_perf", 150f),
                Column.apply("value3m").flex(1), Column.apply("value1y").flex(1),
                Column.apply("value3y").flex(1), Column.apply("value5y").flex(1),
                Column.apply("value10y").flex(1), Column.apply("annualized").flex(1));
        ReportMargin accountPerf = reportRow.getColumnBound("account_perf");
        ReportMargin value3m = reportRow.getColumnBound("value3m");
        ReportMargin value1y = reportRow.getColumnBound("value1y");
        ReportMargin value3y = reportRow.getColumnBound("value3y");
        ReportMargin value5y = reportRow.getColumnBound("value5y");
        ReportMargin value10y = reportRow.getColumnBound("value10y");
        ReportMargin annualized = reportRow.getColumnBound("annualized");
        ReportCell h_accountPerf = new ReportCell(new ReportTxt("Account performance").bold().color(headerFontColor)).leftAlign().
                inside(accountPerf);
        ReportCell h_value3m = new ReportCell(new ReportTxt("3 Months (%)").bold().color(headerFontColor)).rightAlign().
                inside(value3m);
        ReportCell h_value1y = new ReportCell(new ReportTxt("1 Year (%)").bold().color(headerFontColor)).rightAlign().
                inside(value1y);
        ReportCell h_value3y = new ReportCell(new ReportTxt("3 Years (%)").bold().color(headerFontColor)).rightAlign().
                inside(value3y);
        ReportCell h_value5y = new ReportCell(new ReportTxt("5 Years (%)").bold().color(headerFontColor)).rightAlign().
                inside(value5y);
        ReportCell h_value10y = new ReportCell(new ReportTxt("10 Years (%)").bold().color(headerFontColor)).rightAlign().
                inside(value10y);
        ReportCell h_annualized = new ReportCell(new ReportTxt("Annualized since " + sd.format(date1) + " (%)").bold().
                color(headerFontColor)).rightAlign().inside(annualized);
        ReportCell[] hrow = new ReportCell[]{h_accountPerf, h_value3m, h_value1y, h_value3y, h_value5y, h_value10y, h_annualized};
        Float y1 = report.calculate(hrow);
        Float top = report.getY() - report.lineHeight();
        Float bottom = y1 + 2;
        report.rectangle().from(9, top).to(report.pageLayout().width() - 9, bottom).fillColor(headerColor).draw();
        report.print(CellAlign.CENTER, top, bottom, hrow);
        report.setYPosition(y1);
        report.nextLine();

        ReportCell r_accountPerf = new ReportCell(new ReportTxt("Your personal rate of return")).
                leftAlign().inside(accountPerf);
        ReportCell r_value3m = new ReportCell(new ReportTxt(GroupUtilDefs.getRecordValue(record, "value3m").toString())).
                rightAlign().inside(value3m);
        ReportCell r_value1y = new ReportCell(new ReportTxt(GroupUtilDefs.getRecordValue(record, "value1y").toString())).
                rightAlign().inside(value1y);
        ReportCell r_value3y = new ReportCell(new ReportTxt(GroupUtilDefs.getRecordValue(record, "value3y").toString())).
                rightAlign().inside(value3y);
        ReportCell r_value5y = new ReportCell(new ReportTxt(GroupUtilDefs.getRecordValue(record, "value5y").toString())).
                rightAlign().inside(value5y);
        ReportCell r_value10y = new ReportCell(new ReportTxt(GroupUtilDefs.getRecordValue(record, "value10y").toString())).
                rightAlign().inside(value10y);
        ReportCell r_annualized = new ReportCell(new ReportTxt(GroupUtilDefs.getRecordValue(record, "annualized").toString())).
                rightAlign().inside(annualized);
        ReportCell[] rrow = new ReportCell[]{r_accountPerf, r_value3m, r_value1y, r_value3y, r_value5y, r_value10y, r_annualized};
        Float y2 = report.calculate(rrow);
        report.print(rrow);
        report.setYPosition(y2);
        report.nextLine();
    }

    private void disclaimer(Report report) throws Exception {
        report.nextPage();
        report.nextLine(3);
        report.print(new ReportCell(new ReportTxt("Disclaimer").bold().size(20)).at(50));
        report.nextLine(2);
        List<String> txtList = Arrays.asList(
                "Lorem ipsum dolor sit amet, quo consul dolores te, et modo timeam assentior mei. Eos et sonet soleat copiosae. Malis labitur constituam cu cum. Qui unum probo an. Ne verear dolorem quo, sed mediocrem hendrerit id. In alia persecuti nam, cum te equidem elaboraret.",
                "Sint definiebas eos ea, et pri erroribus consectetuer. Te duo veniam iracundia. Utinam diceret efficiendi ad has. Ad mei saepe aliquam electram, sit ne nostro mediocrem neglegentur. Probo adhuc hendrerit nam at, te eam exerci denique appareat.",
                "Eu quem patrioque his. Brute audire equidem sit te, accusam philosophia at vix. Ea invenire inimicus prodesset his, has sint dicunt quaerendum id. Mei reque volutpat quaerendum an, an numquam graecis fierent mel, vim nisl soleat vivendum ut. Est odio legere saperet ad. Dolor invidunt in est.",
                "Porro accumsan lobortis no mea, an harum impetus invenire mei. Sed scaevola insolens voluptatibus ad. Eu aeque dicunt lucilius sit, no nam nullam graecis. Ad detracto deserunt cum, qui nonumy delenit invidunt ne. Per eu nulla soluta verear, in purto homero phaedrum vel, usu ut quas deserunt. Sed abhorreant neglegentur ea, tantas dicunt aliquam mei eu.",
                "Dico fabulas ea est, oporteat scribentur cum ea, usu at nominati reprimique. His omnes saperet eu, nec ei mutat facete vituperatoribus. Ius in erant eirmod fierent, nec ex melius tincidunt. Assueverit interesset vel cu, dicam offendit cu pro, natum atomorum omittantur vim ea. Alii eleifend pri at, an autem nonumy est. Alterum suavitate ea has, dicam reformidans sed no.",
                "Per iriure latine regione ei, libris maiorum sensibus ne qui, te iisque deseruisse nam. Cu mel doming ocurreret, quot rebum volumus an per. Nec laudem partem recusabo in, ei animal luptatum mea. Atqui possim deterruisset qui at, cu dolore intellegebat vim. Sit ad intellegebat vituperatoribus, eu dolores salutatus qui, mei at suas option suscipit. Veniam quodsi patrioque cu qui, ornatus voluptua neglegentur cum eu.",
                "Ea sit brute atqui soluta, qui et mollis eleifend elaboraret. Nec ex tritani repudiare. Ne ornatus salutandi disputationi eos. Sed possit omnesque disputationi et, nominavi recusabo vix in, tota recusabo sententiae et cum. Mei cu ipsum euripidis philosophia, vel homero verterem instructior ex.",
                "Ea affert tation nemore mea. Eum oratio invenire accommodare in, at his lorem atqui iriure, ei alii feugait interesset vel. No per tollit detraxit forensibus. Duo ad nonumy officiis argumentum, sea persius moderatius et.",
                "Pro stet oratio exerci in. Per no nullam salutatus scriptorem. Stet alterum nam ei, congue tamquam sed ea. Eam ut virtute disputationi, ea labitur voluptua has. Est ea graecis definitiones, pro ea mutat oportere adipiscing.",
                "Suscipit ponderum verterem et mel, vim semper facilisi ex, mel aliquid constituam ut. Summo denique complectitur ius at, in quo nobis deterruisset. Ut viris convenire eam. Quo id suscipit quaerendum, magna veniam et vix, duis liber disputando et has. Aliquando democritum id usu, falli diceret invidunt in per, in falli essent quo."
        );
        for (String txt : txtList) {
            ReportCell cell = (new ReportCell(new ReportTxt(txt))).inside(new ReportMargin(10, report.pageLayout().width() - 10));
            ReportTypes.WrapBox box = cell.calculate(report);
            report.print(cell);
            report.setYPosition(box.currentY() + report.lineHeight());
            if (report.lineLeft() < 5) {
                report.nextPage();
            }
        }
    }

    public static void main(String[] args) throws Exception {
        MutualFundsInitData.initDb();
        new MutualFundsNoAkkaJavaReport().run();
    }
}

class MemoryPersistenceUtil implements PersistenceUtil {
    private HashMap<Long, byte[]> map = new HashMap<Long, byte[]>();

    @Override
    public void writeObject(long key, byte[] obj) {
        map.put(key, obj);
    }

    @Override
    public byte[] readObject(long key) {
        return map.get(key);
    }

    @Override
    public List<Long> getAllKeys() {
        List<Long> result = new ArrayList<Long>();
        result.addAll(map.keySet());
        Collections.sort(result);
        return result;
    }

    @Override
    public void open() {

    }

    @Override
    public void close() {
        map.clear();
    }
}

class DerbyPersistenceUtil implements PersistenceUtil {
    private final String driver = "org.apache.derby.jdbc.EmbeddedDriver";
    private final String dbFolder = System.getProperty("java.io.tmpdir");
    private final String prefix = "persistence";
    private final String extension = ".db";
    private String dbPath = "";
    private Connection conn = null;
    private File file = null;

    public DerbyPersistenceUtil() {
        try {
            Class.forName(driver).newInstance();
            file = File.createTempFile(prefix, extension, new File(dbFolder));
            file.delete();
            dbPath = file.getAbsolutePath();
            conn = DriverManager.getConnection("jdbc:derby:" + dbPath + ";create=true");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean exists(Long key) {
        PreparedStatement stmnt = null;
        ResultSet rs = null;
        try {
            stmnt = conn.prepareStatement("select count(*) from persist where id=" + key);
            rs = stmnt.executeQuery();
            boolean result = false;
            if (rs.next()) {
                result = rs.getLong(1) > 0;
            }
            rs.close();
            stmnt.close();
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (stmnt != null) {
                try {
                    stmnt.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    @Override
    public void writeObject(long key, byte[] obj) {
        PreparedStatement ps = null;
        try {
            boolean keyExists = exists(key);
            int keyPos = 0;
            int blobPos = 0;
            if (keyExists) {
                ps = conn.prepareStatement("update persist set content=? where id=?");
                keyPos = 2;
                blobPos = 1;
            } else {
                ps = conn.prepareStatement("insert into persist(id,content)  values(?,?)");
                keyPos = 1;
                blobPos = 2;
            }
            Blob blob = conn.createBlob();
            blob.setBytes(1, obj);
            ps.setLong(keyPos, key);
            ps.setBlob(blobPos, blob);
            ps.execute();
            blob.free();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (ps != null) {
                try {
                    ps.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public byte[] readObject(long key) {
        PreparedStatement stmnt = null;
        ResultSet rs = null;
        byte[] result = null;
        try {
            stmnt = conn.prepareStatement("select content from persist where id=" + key);
            rs = stmnt.executeQuery();
            if (rs.next()) {
                Blob aBlob = rs.getBlob(1);
                result = aBlob.getBytes(1, (int) aBlob.length());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (stmnt != null) {
                try {
                    stmnt.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    @Override
    public List<Long> getAllKeys() {
        PreparedStatement stmnt = null;
        ResultSet rs = null;
        List<Long> result = new ArrayList<Long>();
        try {
            stmnt = conn.prepareStatement("select id from persist order by id");
            rs = stmnt.executeQuery();
            while (rs.next()) {
                result.add(rs.getLong(1));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (stmnt != null) {
                try {
                    stmnt.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    @Override
    public void open() {
        Statement stmt = null;
        try {
            stmt = conn.createStatement();
            stmt.executeUpdate("create table persist (id BIGINT primary key, content blob(2G))");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void close() {
        if (conn != null) {
            try {
                conn.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        for (File fileItem : file.listFiles()) {
            fileItem.delete();
        }
        file.delete();
    }
}