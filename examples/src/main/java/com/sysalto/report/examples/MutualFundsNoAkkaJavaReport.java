package com.sysalto.report.examples;

import com.sysalto.render.PdfNativeFactory;
import com.sysalto.report.Report;
import com.sysalto.report.ReportTypes;
import com.sysalto.report.examples.mutualFunds.MutualFundsInitData;
import com.sysalto.report.reportTypes.*;
import com.sysalto.report.util.GroupUtilDefs;
import com.sysalto.report.util.GroupUtilTrait;
import com.sysalto.report.util.PdfFactory;
import com.sysalto.report.util.ResultSetGroup;
import scala.collection.immutable.Map;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
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
    static RColor headerColor = new RColor(156, 76, 6, 1f);
    static RColor headerFontColor = new RColor(255, 255, 255, 1f);

    static private final SimpleDateFormat sd = new SimpleDateFormat("MMM dd yyyy");

    PdfFactory pdfITextFactory = new PdfNativeFactory();

    private void run() throws Exception {

        Report report = Report.create("MutualFundsJava.pdf", ReportPageOrientation.LANDSCAPE(), pdfITextFactory);
        report.headerSizeCallback(pg -> {
            Long pgNbr = new Long(pg.toString());
            if (pgNbr == 1) return 0f;
            else return 50f;
        });
        report.footerSizeCallback(pg -> 30f);

        report.headerFct((pg, pgMax) -> {
            report.setYPosition(10);
            Row row = Row.apply(10.f, report.pgSize().width() - 10, Column.apply("column1").flex(1),
                    Column.apply("column2").flex(1), Column.apply("column3").flex(1));
            RMargin column1 = row.getColumnBound("column1");
            RMargin column2 = row.getColumnBound("column2");
            RMargin column3 = row.getColumnBound("column3");
            RCell h_column1 = new RCell(new RText("Type of Account").bold()).leftAlign().inside(column1);
            RCell h_column2 = new RCell(new RText("Your account number").bold()).leftAlign().inside(column2);
            RCell h_column3 = new RCell(new RText("Your investment statement").bold()).rightAlign().inside(column3);
            RRow hrow = RRow.apply(h_column1, h_column2, h_column3);
            report.print(hrow);
            report.nextLine();
            String str = sd.format(date1) + " to " + sd.format(date2);
            RCell r_column1 = new RCell(new RText("Group Registered Retirement Saving Plan")).leftAlign().inside(column1);
            RCell r_column2 = new RCell(new RText("123456789")).leftAlign().inside(column2);
            RCell r_column3 = new RCell(new RText(str)).rightAlign().inside(column3);
            RRow rrow = RRow.apply(r_column1, r_column2, r_column3);
            report.print(rrow);
            report.nextLine(2);
            report.line().from(10, report.getY()).to(report.pgSize().width() - 10, -1).draw();
        });

        report.footerFct((pg, pgMax) -> {
            report.setYPosition(report.pgSize().height() - report.lineHeight() * 3);
            report.line().from(10, report.getY()).to(report.pgSize().width() - 10, -1).draw();
            report.nextLine();
            RCell cell = new RCell(new RText("Page " + pg + " of " + pgMax).bold()).rightAlign().inside(0, report.pgSize().width() - 10);
            report.print(cell);
        });
        reportHeader(report);
        summaryOfInvestment(report);
        changeAccount(report);
        accountPerformance(report);
        disclaimer(report);
        report.render();
    }


    private void reportHeader(Report report) throws Exception {
        drawbackgroundImage(report);
        ResultSet rs = MutualFundsInitData.query("select * from clnt");
        rs.next();

        Map<String, Object> record = GroupUtilDefs.toMap(rs);
        rs.close();
        report.nextLine();
        report.drawImage("examples/src/main/resources/images/bank_banner.jpg", 5f, 45f, 100f, 40f);
        RMargin margin = new RMargin(0, report.pgSize().width() - 10);
        report.print(new RCell(new RText("Investment statement").size(15).bold()).rightAlign().inside(margin));
        report.nextLine();
        String str = sd.format(date1) + " to " + sd.format(date2);
        report.print(new RCell(new RText(str).size(15).bold()).rightAlign().inside(margin));
        report.nextLine(2);
        report.print(new RCell(new RText("Mutual Funds Inc.").bold()).at(10));
        report.nextLine();
        report.print(new RCell(new RText("Group Registered Retirement Saving Plan").bold()).at(10));
        report.nextLine(2);
        Float y = report.getY();
        report.print(new RCell(new RText(GroupUtilDefs.getRecordValue(record, "name").toString()).bold()).at(10));
        report.nextLine();
        report.print(new RCell(new RText(GroupUtilDefs.getRecordValue(record, "addr1").toString())).at(10));
        report.nextLine();
        report.print(new RCell(new RText(GroupUtilDefs.getRecordValue(record, "addr2").toString())).at(10));
        report.nextLine();
        report.print(new RCell(new RText(GroupUtilDefs.getRecordValue(record, "addr3").toString())).at(10));
        report.setYPosition(y);
        report.print(new RCell(new RText("Beneficiary information").bold()).at(500));
        report.nextLine();
        report.print(new RCell(new RText(GroupUtilDefs.getRecordValue(record, "benef_name").toString())).at(500));
        report.nextLine(2);
    }


    private void summaryOfInvestment(Report report) throws Exception {

        report.nextLine(2);
        Row row = Row.apply(10.f, report.pgSize().width() - 10, Column.apply("fund_name", 150f),
                Column.apply("value1").flex(1), Column.apply("value2").flex(1), Column.apply("change").flex(1),
                Column.apply("graphic").flex(2));
        RMargin m_fundName = row.getColumnBound("fund_name");
        RMargin m_value1 = row.getColumnBound("value1");
        RMargin m_value2 = row.getColumnBound("value2");
        RMargin m_change = row.getColumnBound("change");
        RMargin m_graphic = row.getColumnBound("graphic");
        RCell c_fundName = new RCell(new RText("Summary of investments").bold().color(headerFontColor)).leftAlign().
                inside(m_fundName);
        RCell c_value1 = new RCell(new RText("Value on\n" + sd.format(date1) + "($$)").bold().color(headerFontColor)).rightAlign().
                inside(m_value1);
        RCell c_value2 = new RCell(new RText("Value on\n" + sd.format(date2) + "($$)").bold().color(headerFontColor)).rightAlign().
                inside(m_value2);
        RCell c_change = new RCell(new RText("Change($$)").bold().color(headerFontColor)).rightAlign().inside(m_change);
        RCell c_graphic = new RCell(new RText("Assets mix\n" + sd.format(date2) + "(%)").bold().color(headerFontColor)).rightAlign().inside(m_graphic);
        RRow rrow = RRow.apply(c_fundName, c_value1, c_value2, c_change, c_graphic);
        Float y2 = rrow.calculate(report);
        report.rectangle().from(9, report.getY() - report.lineHeight()).radius(3).to(report.pgSize().width() - 9, y2 + 2).fillColor(headerColor).draw();


        report.print(rrow);
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
            RTextList fundTxt = new RText(cc + " ").bold().plus(new RText(fund_name));
            RCell cr_fundName = RCell.apply(fundTxt).leftAlign().inside(m_fundName);
            RCell cr_value1 = new RCell(new RText(value1.toString())).rightAlign().inside(m_value1);
            RCell cr_value2 = new RCell(new RText(value2.toString())).rightAlign().inside(m_value2);
            Float v_change = value2.floatValue() - value1.floatValue();
            total1.set(total1.get() + value1.floatValue());
            total2.set(total2.get() + value2.floatValue());
            total3.set(total3.get() + v_change.floatValue());

            chartData.get().add(new scala.Tuple2("" + cc, total2.get()));
            RCell cr_change = new RCell(new RText(v_change.toString())).rightAlign().inside(m_change);
            RRow rrow1 = RRow.apply(cr_fundName, cr_value1, cr_value2, cr_change);
            Float y3 = rrow1.calculate(report);
            report.print(rrow1);

            if (GroupUtil.isLastRecord(rec)) {
                report.line().from(10, report.getY() + 2).to(m_change.right(), -1).width(0.1f).draw();
            } else {
                report.line().from(10, report.getY() + 2).to(m_change.right(), -1).color(200, 200, 200).lineType(new LineDashType(2, 1)).width(0.5f).draw();
            }
            firstChar.set(firstChar.get() + 1);
            report.nextLine();
        });


        RRow trow = RRow.apply(new RCell(new RText("Total").bold()).inside(m_fundName),
                new RCell(new RText(total1.toString()).bold()).rightAlign().inside(m_value1),
                new RCell(new RText(total2.toString()).bold()).rightAlign().inside(m_value2),
                new RCell(new RText(total3.toString()).bold()).rightAlign().inside(m_change));
        report.print(trow);
        float chartHeight = report.getY() - firstY.get() - 10;
        report.drawPieChart1("", chartData.get(), m_graphic.left() + 5, firstY.get() - report.lineHeight() + 5, m_graphic.right() -
                m_graphic.left() - 10, chartHeight);
    }

    private void drawbackgroundImage(Report report) {
        report.rectangle().from(0, 0).to(report.pgSize().width(), report.pgSize().height()).
                verticalShade(new RColor(255, 255, 255, 1), new RColor(255, 255, 180, 1)).draw();
    }


    private void changeAccount(Report report) throws Exception {
        report.nextLine(2);
        Row row = Row.apply(10.f, report.pgSize().width() - 10, Column.apply("account", 250f),
                Column.apply("value1").flex(1), Column.apply("value2").flex(1), Column.apply("value3").flex(1));
        RMargin account = row.getColumnBound("account");
        RMargin value1 = row.getColumnBound("value1");
        RMargin value2 = row.getColumnBound("value2");
        RMargin value3 = row.getColumnBound("value3");
        RCell accountHdr = new RCell(new RText("Change in the value of account").bold().
                color(headerFontColor)).leftAlign().inside(account);
        RCell value1Hdr = new RCell(new RText("This period($)").bold().
                color(headerFontColor)).rightAlign().inside(value1);
        RCell value2Hdr = new RCell(new RText("Year-to-date($)").bold().
                color(headerFontColor)).rightAlign().inside(value2);
        RCell value3Hdr = new RCell(new RText("Since\n" + sd.format(date1) + "($)").bold().
                color(headerFontColor)).rightAlign().inside(value3);
        RRow rrow = RRow.apply(accountHdr, value1Hdr, value2Hdr, value3Hdr);
        float y2 = rrow.calculate(report);
        report.rectangle().from(9, report.getY() - report.lineHeight()).radius(3).to(report.pgSize().width() - 9, y2 + 2).fillColor(headerColor).draw();
        report.print(rrow);
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
            Map<String, Object> crtRec = GroupUtil.<scala.collection.immutable.Map<java.lang.String,java.lang.Object>>getRec(rec);
            String name = GroupUtilDefs.getRecordValue(crtRec, "name");
            BigDecimal r_value1 = GroupUtilDefs.getRecordValue(crtRec, "value1");
            BigDecimal r_value2 = GroupUtilDefs.getRecordValue(crtRec, "value2");
            BigDecimal r_value3 = GroupUtilDefs.getRecordValue(crtRec, "value3");
            RCell c_account = new RCell(new RText(name)).leftAlign().inside(account);
            RCell c_value1 = new RCell(new RText(r_value1.toString())).rightAlign().inside(value1);
            RCell c_value2 = new RCell(new RText(r_value2.toString())).rightAlign().inside(value2);
            RCell c_value3 = new RCell(new RText(r_value3.toString())).rightAlign().inside(value3);
            total1.set(total1.get() + r_value1.doubleValue());
            total2.set(total2.get() + r_value2.doubleValue());
            total3.set(total3.get() + r_value3.doubleValue());
            RRow rrow1 = RRow.apply(c_account, c_value1, c_value2, c_value3);
            Float y21 = rrow1.calculate(report);
            report.print(rrow1);
            RColor rcolor = null;
            if (GroupUtil.isLastRecord(rec)) {
                rcolor = new RColor(0, 0, 0, 1f);
            } else {
                rcolor = new RColor(200, 200, 200, 1f);
            }
            report.line().from(10, report.getY() + 2).to(value3.right(), -1).color(rcolor).width(0.5f).draw();
            report.nextLine();
        });

        rs.close();
        RCell accountSum = new RCell(new RText("Value of  account on " + sd.format(date2)).bold()).leftAlign().inside(account);
        RCell value1Sum = new RCell(new RText("" + total1.get()).bold()).rightAlign().inside(value1);
        RCell value2Sum = new RCell(new RText("" + total2.get()).bold()).rightAlign().inside(value2);
        RCell value3Sum = new RCell(new RText("" + total3.get()).bold()).rightAlign().inside(value3);
        RRow frow = RRow.apply(accountSum, value1Sum, value2Sum, value3Sum);
        Float y3 = frow.calculate(report);
        report.print(frow);
        report.setYPosition(y3);
        report.nextLine();
    }

    private void accountPerformance(Report report) throws Exception {
        ResultSet rs = MutualFundsInitData.query("select * from account_perf");
        rs.next();
        Map<String, Object> record = GroupUtilDefs.toMap(rs);
        rs.close();
        Row row = Row.apply(10.f, report.pgSize().width() - 10, Column.apply("account_perf", 150f),
                Column.apply("value3m").flex(1), Column.apply("value1y").flex(1),
                Column.apply("value3y").flex(1), Column.apply("value5y").flex(1),
                Column.apply("value10y").flex(1), Column.apply("annualized").flex(1));
        RMargin accountPerf = row.getColumnBound("account_perf");
        RMargin value3m = row.getColumnBound("value3m");
        RMargin value1y = row.getColumnBound("value1y");
        RMargin value3y = row.getColumnBound("value3y");
        RMargin value5y = row.getColumnBound("value5y");
        RMargin value10y = row.getColumnBound("value10y");
        RMargin annualized = row.getColumnBound("annualized");
        RCell h_accountPerf = new RCell(new RText("Account performance").bold().color(headerFontColor)).leftAlign().
                inside(accountPerf);
        RCell h_value3m = new RCell(new RText("3 Months (%)").bold().color(headerFontColor)).rightAlign().
                inside(value3m);
        RCell h_value1y = new RCell(new RText("1 Year (%)").bold().color(headerFontColor)).rightAlign().
                inside(value1y);
        RCell h_value3y = new RCell(new RText("3 Years (%)").bold().color(headerFontColor)).rightAlign().
                inside(value3y);
        RCell h_value5y = new RCell(new RText("5 Years (%)").bold().color(headerFontColor)).rightAlign().
                inside(value5y);
        RCell h_value10y = new RCell(new RText("10 Years (%)").bold().color(headerFontColor)).rightAlign().
                inside(value10y);
        RCell h_annualized = new RCell(new RText("Annualized since " + sd.format(date1) + " (%)").bold().
                color(headerFontColor)).rightAlign().inside(annualized);
        RRow hrow = RRow.apply(h_accountPerf, h_value3m, h_value1y, h_value3y, h_value5y, h_value10y, h_annualized);
        Float y1 = hrow.calculate(report);
        report.rectangle().from(9, report.getY()).to(report.pgSize().width() - 9, y1 + 2).fillColor(headerColor).draw();
        report.print(hrow);
        report.setYPosition(y1);
        report.nextLine();

        RCell r_accountPerf = new RCell(new RText("Your personal rate of return")).
                leftAlign().inside(accountPerf);
        RCell r_value3m = new RCell(new RText(GroupUtilDefs.getRecordValue(record, "value3m").toString())).
                rightAlign().inside(value3m);
        RCell r_value1y = new RCell(new RText(GroupUtilDefs.getRecordValue(record, "value1y").toString())).
                rightAlign().inside(value1y);
        RCell r_value3y = new RCell(new RText(GroupUtilDefs.getRecordValue(record, "value3y").toString())).
                rightAlign().inside(value3y);
        RCell r_value5y = new RCell(new RText(GroupUtilDefs.getRecordValue(record, "value5y").toString())).
                rightAlign().inside(value5y);
        RCell r_value10y = new RCell(new RText(GroupUtilDefs.getRecordValue(record, "value10y").toString())).
                rightAlign().inside(value10y);
        RCell r_annualized = new RCell(new RText(GroupUtilDefs.getRecordValue(record, "annualized").toString())).
                rightAlign().inside(annualized);
        RRow rrow = RRow.apply(r_accountPerf, r_value3m, r_value1y, r_value3y, r_value5y, r_value10y, r_annualized);
        Float y2 = rrow.calculate(report);
        report.print(rrow);
        report.setYPosition(y2);
        report.nextLine();
    }

    private void disclaimer(Report report) throws Exception {
        report.nextPage();
        drawbackgroundImage(report);
        report.nextLine();
        report.print(new RCell(new RText("Disclaimer").bold().size(20)).at(50));
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
            RCell cell = (new RCell(new RText(txt))).inside(new RMargin(10, report.pgSize().width() - 10));
            ReportTypes.WrapBox box = cell.calculate(report);
            report.print(cell);
            report.setYPosition(box.currentY() + report.lineHeight());
            if (report.lineLeft() < 10) {
                report.nextPage();
            }
        }
    }

    public static void main(String[] args) throws Exception {
        MutualFundsInitData.initDb();
        new MutualFundsNoAkkaJavaReport().run();
    }
}
