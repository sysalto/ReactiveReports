package com.sysalto.report.examples;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Sink;
import com.sysalto.render.PdfITextFactory;
import com.sysalto.report.Report;
import com.sysalto.report.reportTypes.*;
import com.sysalto.report.util.GroupTransform;
import com.sysalto.report.util.ReportColumnUtil;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import akka.stream.javadsl.*;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletionStage;

/**
 * Created by marian on 2/11/17.
 */
public class HelloWorldJavaReport {


    private void run() {
        PdfITextFactory pdfITextFactory = new PdfITextFactory();
        Config config = ConfigFactory.parseString(
                "akka.log-dead-letters=off\n" +
                        "akka.jvm-exit-on-fatal-error = true\n" +
                        "akka.log-dead-letters-during-shutdown=off");
        ActorSystem system = ActorSystem.create("Sys", config);
        ActorMaterializer materializer = ActorMaterializer.create(system);
        Report report = Report.create("HellowWorldJava.pdf", system, materializer, pdfITextFactory);


        report.getFooterSize(pg -> 30f);
        report.footerFct((pg,pgMax) -> {
            report.setYPosition(report.pgSize().height() - report.lineHeight() * 2);
            report.line().from(10, report.getY()).to(report.pgSize().width() - 10, -1).draw();
            report.setYPosition(report.getY() + report.lineHeight() * 0.5f);
            RCell cell = new RCell(new RText("Page " + pg + " of " + pgMax).bold(),
                    0.f, report.pgSize().width() - 10).rightAllign();
            report.print(cell);
        });

        List<TestRecord> records = Arrays.asList(new TestRecord("Toronto", "name1", "addr1"),
                new TestRecord("Toronto", "name2", "addr2"),
                new TestRecord("Montreal", "name2", "addr2"));
        Source<TestRecord, NotUsed> reportSource = Source.from(records);

        GroupUtil<TestRecord> reportGroupUtil = GroupUtil.apply(new Group<TestRecord>("city", (TestRecord r) -> r.city));

        // print the header
        List<Column> columnList = Arrays.asList(Column.apply("name", 200.f), Column.apply("address").flex(1));
        Row row = Row.apply(10.f, report.pgSize().width() - 10, columnList);
        RMargin nameC = row.getColumnBound("name");
        RMargin addressC = row.getColumnBound("address");
        RCell h_row = new RCell(new RText("name").bold()).leftAllign().between(nameC);
        RCell h_address = new RCell(new RText("address").bold()).leftAllign().between(addressC);
        RRow hrow = RRow.apply(h_row, h_address);


        report.nextLine();

        CompletionStage result = (CompletionStage) reportSource.via(new GroupTransform()).runWith(Sink.foreach(
                rec1 -> {
                    TestRecord currentRecord = reportGroupUtil.getRec(rec1);
                    boolean isHeader = reportGroupUtil.isHeader("city", rec1);
                    boolean newPageForCity = false;
                    if (!reportGroupUtil.isFirstRecord(rec1) && reportGroupUtil.isHeader("city", rec1)) {
                        report.newPage();
                        newPageForCity = true;
                    }
                    if (reportGroupUtil.isFirstRecord(rec1)) {
                        newPageForCity = true;
                    }
                    if (reportGroupUtil.isHeader("city", rec1)) {
                        report.text("City:" + currentRecord.city, 10f);
                        report.nextLine();
                        hrow.print(report);
                        report.line().from(10, report.getY()).to(report.pgSize().width() - 10, -1).draw();
                        report.nextLine();
                    }
                    RCell name = new RCell(new RText(currentRecord.name)).leftAllign().between(nameC);
                    RCell address = new RCell(new RText(currentRecord.address)).leftAllign().between(addressC);
                    RRow rrow = RRow.apply(name, address);
                    if (report.lineLeft() < 5) {
                        report.newPage();
                        if (!isHeader) {
                            report.nextLine();
                        }
                    }
                    rrow.print(report);
                    report.nextLine();
                    System.out.println(currentRecord);
                }), materializer);

        try {
            result.toCompletableFuture().get();
            report.render();
            report.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        system.terminate();
    }


    class TestRecord {
        String city, name, address;

        public TestRecord(String city, String name, String address) {
            this.city = city;
            this.name = name;
            this.address = address;
        }
    }

    public static void main(String[] args) {
        new HelloWorldJavaReport().run();
    }
}
