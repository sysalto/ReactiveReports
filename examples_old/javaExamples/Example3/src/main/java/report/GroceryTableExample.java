package report;

import com.sysalto.render.PdfNativeFactory;
import com.sysalto.report.Report;
import com.sysalto.report.reportTypes.*;
import com.sysalto.report.util.PdfFactory;

import java.util.ArrayList;
import java.util.List;


class Food {
    private String name;
    private Integer price;
    private String quantity;

    public Food(String name, Integer price, String quantity) {
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

    public String getQuanitty() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }
}
public class GroceryTableExample {


    private void report() throws Exception {

        List<Food> foodList= new ArrayList<Food>();

        // Populating the food list
        foodList.add(new Food("Beef", 10, "2 kg"));
        foodList.add(new Food("Tomatoes", 5, "1 pack"));
        foodList.add(new Food("Pizza", 7, "1 medium"));
        foodList.add(new Food("Potatoes", 6, "3 kg"));
        foodList.add(new Food("Chocolate", 3, "2"));
        foodList.add(new Food("Cheesecake", 30, "1"));
        foodList.add(new Food("Coffee", 20, "1 pack"));
        foodList.add(new Food("Fish", 12, "1 kg"));
        foodList.add(new Food("Water", 15, "5 liters"));
        foodList.add(new Food("Orange Juice", 4, "2 bottles"));
        foodList.add(new Food("Bread", 4, "3 loaves"));
        foodList.add(new Food("Rice", 4, "2 kg"));

        PdfFactory pdfFactory = new PdfNativeFactory();
        Report report = Report.create("GroceryReport.pdf", ReportPageOrientation.PORTRAIT(), pdfFactory);
        Row row = Row.apply(50, report.pgSize().width() - 50, Column.apply("name").flex(1),
                Column.apply("price").flex(1), Column.apply("quantity").flex(1));
        RMargin name = row.getColumnBound("name");
        RMargin quantity  = row.getColumnBound("quantity");
        RMargin price = row.getColumnBound("price");
        report.nextLine(10);

        RCell h_name = new RCell(new RText("Name").bold()).leftAlign().inside(name);
        RCell h_price = new RCell(new RText("Price/Unit").bold()).rightAlign().inside(price);
        RCell h_quantity = new RCell(new RText("Quantity").bold()).rightAlign().inside(quantity);
        RRow hrow = RRow.apply(h_name, h_price, h_quantity);
        hrow.print(report);
        report.nextLine();
        report.line().from(40, report.getY()).to(report.pgSize().width() - 40, -1).draw();
        report.nextLine();

        for (Food food : foodList) {
            RCell v_name = new RCell(new RText(food.getName())).leftAlign().inside(name);
            RCell v_price = new RCell(new RText("" + food.getPrice())).rightAlign().inside(price);
            RCell v_quantity = new RCell(new RText(food.getQuanitty())).rightAlign().inside(quantity);
            RRow vrow = RRow.apply(v_name, v_price, v_quantity);
            vrow.print(report);
            report.nextLine();
            if (report.lineLeft()<5) {
                report.nextPage();
            }
        }
        report.render();
    }

    private void drawbackgroundImage(Report report) {
        report.rectangle().from(0, 0).to(report.pgSize().width(), report.pgSize().height()).
                verticalShade(new RColor(255, 255, 255, 1), new RColor(255, 255, 180, 1)).draw();
    }

    public static void main(String[] args) throws Exception {
        GroceryTableExample groceryReport = new GroceryTableExample();
        groceryReport.report();
    }

}
