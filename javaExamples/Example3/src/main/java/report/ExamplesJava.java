package report;
import com.sysalto.render.PdfNativeFactory;
import com.sysalto.report.Report;
import com.sysalto.report.ReportTypes;
import com.sysalto.report.reportTypes.*;
import com.sysalto.report.util.PdfFactory;

import java.io.File;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

class ShoppingItem {
    private String name;
    private Integer price;
    private Integer quantity;

    public ShoppingItem(String name, Integer price, Integer quantity) {
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

    public Integer getQuanitty() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}

public class ExamplesJava {

    // Example 1 - Simple report - one line - PORTRAIT orientation
    private void example_1() throws Exception {
        PdfFactory pdfFactory = new PdfNativeFactory();

        Report report1 = Report.create("PDF_portrait.pdf", ReportPageOrientation.PORTRAIT(), pdfFactory);
        report1.nextLine();
        report1.print(new RText("line1 portrait")).at(50);
        report1.render();
    }

    // Example 2 - Simple report - one line - LANDSCAPE orientation
    private void example_2() throws Exception {
        PdfFactory pdfFactory = new PdfNativeFactory();

        Report report2 = Report.create("PDF_landscape.pdf", ReportPageOrientation.LANDSCAPE(), pdfFactory);
        report2.nextLine();
        report2.print(new RText("line2 landscape")).at(50);
        report2.render();
    }

    // Example 3 - report with summary, the summary has a link to the next page
    private void example_3() throws Exception {
        PdfFactory pdfFactory = new PdfNativeFactory();

        Report reportSumamry = Report.create("PDF_reportSummary.pdf", ReportPageOrientation.PORTRAIT(), pdfFactory);
        reportSumamry.nextLine();

        // boundRect is the rectangle boundary around the text
        ReportTypes.BoundaryRect boundRect = reportSumamry.print(new RText("GoTo Page2").italic()).at(50);

        // We set the link, so when the user clicks on the text, it sends you to page 2
        reportSumamry.setLinkToPage(boundRect,2, 0, 0);
        reportSumamry.nextPage();
        reportSumamry.nextLine();

        // Contents of page 2
        reportSumamry.print(new RText("Page2")).at(50);

        reportSumamry.render();
    }

    /*
     * Example 4 - report with graphics
     * Includes background colour in gradient, logo at the top left corner,
     * header and footer, content on each page; Title on first page
     */
    private void example_4() throws Exception {
        PdfFactory pdfFactory = new PdfNativeFactory();
        Report report = Report.create("PDF_withGraphics.pdf", ReportPageOrientation.PORTRAIT(), pdfFactory);

        report.headerSizeCallback(pg -> {
            Long pgNbr = new Long(pg.toString());
            if (pgNbr == 1) return 0f;
            else return 50f;
        });
        report.footerSizeCallback(pg -> {
            Long pgNbr = new Long(pg.toString());
            if (pgNbr == 1) return 0f;
            else return 30f;
        });

        List<ShoppingItem> shoppingList= new ArrayList<ShoppingItem>();

        // Populating the food list
        shoppingList.add(new ShoppingItem("Beef", 10, 2));
        shoppingList.add(new ShoppingItem("Tomatoes", 5, 1));
        shoppingList.add(new ShoppingItem("Pizza", 7, 1));
        shoppingList.add(new ShoppingItem("Potatoes", 6, 3 ));
        shoppingList.add(new ShoppingItem("Chocolate", 3, 2));
        shoppingList.add(new ShoppingItem("Cheesecake", 30, 1));
        shoppingList.add(new ShoppingItem("Coffee", 20, 1));
        shoppingList.add(new ShoppingItem("Fish", 12, 1));
        shoppingList.add(new ShoppingItem("Water", 1, 5));
        shoppingList.add(new ShoppingItem("Orange Juice", 4, 2));
        shoppingList.add(new ShoppingItem("Bread", 4, 3));
        shoppingList.add(new ShoppingItem("Rice", 4, 2));


        // Header
        report.headerFct((pg, pgMax) -> {
            report.setYPosition(10);
            report.nextLine(2);
            report.print(new RText("Header: Grocery list for holidays - Costco")).at(100);
        });

        // Footer
        report.footerFct((pg, pgMax) -> {
            report.setYPosition(report.pgSize().height() - report.lineHeight() * 3);
            report.line().from(10, report.getY()).to(report.pgSize().width() - 10, -1).draw();
            report.nextLine();
            RCell cell = new RCell(new RText("Page " + pg + " of " + pgMax).bold()).rightAlign().inside(0, report.pgSize().width() - 10);
            report.print(cell);
        });

        // Page 1
        // Adding the logo on the top left corner of the report
        URL resource = this.getClass().getResource("/images/Logo-Costco.jpg");
        File file = Paths.get(resource.toURI()).toFile();
        drawbackgroundImage(report);
        report.drawImage(file.getAbsolutePath(), 5f, 45f, 100f, 40f);

        // Title of report on page 1
        report.nextLine(6);
        report.print(new RText("Grocery list").bold().size(15)).at(250);

        report.nextLine(7);



        // Adding the report on page 1
        Row row = Row.apply(50, report.pgSize().width() - 50, Column.apply("name").flex(1),
                Column.apply("price").flex(1), Column.apply("quantity").flex(1));
        RMargin name = row.getColumnBound("name");
        RMargin quantity  = row.getColumnBound("quantity");
        RMargin price = row.getColumnBound("price");


        RCell h_name = new RCell(new RText("Name").bold()).leftAlign().inside(name);
        RCell h_price = new RCell(new RText("Price/Unit").bold()).rightAlign().inside(price);
        RCell h_quantity = new RCell(new RText("Quantity").bold()).rightAlign().inside(quantity);
        RRow hrow = RRow.apply(h_name, h_price, h_quantity);

        //RCell h_Total = new RCell(new RText("Total $:").bold()).rightAlign().inside(name);
        //RRow total = RRow.apply(h_Total);

        hrow.print(report);
        report.nextLine();
        report.line().from(40, report.getY()).to(report.pgSize().width() - 40, -1).draw();
        report.nextLine();

        for (ShoppingItem item : shoppingList) {
            RCell v_name = new RCell(new RText(item.getName())).leftAlign().inside(name);
            RCell v_price = new RCell(new RText("" + item.getPrice())).rightAlign().inside(price);
            RCell v_quantity = new RCell(new RText("" + item.getQuanitty())).rightAlign().inside(quantity);
            RRow vrow = RRow.apply(v_name, v_price, v_quantity);
            vrow.print(report);
            report.nextLine();
            if (report.lineLeft()<5) {
                report.nextPage();
            }
        }

        report.nextPage();


        // Page 2
        report.nextLine(3);
        report.print(new RText("Page 2")).at(50);
        report.nextLine(4);

        // Adding a link to another page
        ReportTypes.BoundaryRect boundRect = report.print(new RText("Click here - go to Page 3")).at(60);
        report.setLinkToPage(boundRect, 3, 0, 0);
        drawbackgroundImage(report);
        report.nextPage();

        // Page 3
        report.nextLine(3);
        report.print(new RText("Page 3")).at(50);
        drawbackgroundImage(report);
        report.render();
    }

    // Drawing the background gradient yellow to white
    private void drawbackgroundImage(Report report) {
        report.rectangle().from(0, 0).to(report.pgSize().width(), report.pgSize().height()).
                verticalShade(new RColor(255, 255, 255, 1),    // white
                        new RColor(255, 255, 180, 1)).draw();  // yellow
    }

    // Running all the example reports
    public static void main(String[] args) throws Exception {
        ExamplesJava myReport = new ExamplesJava();

        myReport.example_1(); // Simple example - portrait
        myReport.example_2(); // Simple example - landscape
        myReport.example_3(); // Report with summary, the summary has a link to the next page
        myReport.example_4(); // Report with graphics
    }
}
