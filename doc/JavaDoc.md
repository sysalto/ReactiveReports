# Reactive Reports Documentation for Java
## Getting started with Reactive Reports
Reactive Reports is a report's framework written in Scala that can be used from any Java ecosystem language 
(Java,Scala,Groovy,etc).
The java version should be at least 8 (because the framework was developed with Scala 2.12 that requires JDK >1.8). 

The core framework has a few dependencies:
* com.typesafe.config library - it is used for easy setup config.
* com.github.romix.akka.akka-kryo-serialization - a very efficient and fast serialization library - 
is is used for marshall/unmarshall data into rocksdb.
* org.rocksdb.rocksdbjni - a super fast key-value store developed by Facebook - is needed for keeping all of the generating pages in memory before rendering.
 
 
 The framework itself is modular.It consists of a core component - reactivereports-core and a 
 pdf render component - reactivereports-pdf-render.
 Also it has another component reactiveReports-core-akka special design for akka stream and big data. 
 
 Let's start with the first java standalone report.It will be a maven report.You can take entire source code from 
 javaExamples/Example1. 
 
 In the pom.xml we have to include reactivereports-core_2.12 (removing unneeded com.typesafe.akka.akka-remote_2.12) 
 and reactivereports-pdf-render_2.12.
 Also we need a simple database - let's use org.hsqldb.hsqldb.
 
 Let's create the Java class Example1InitData that take care of all database staff and create a somple table clnt : 
 create table clnt (groupName varchar(255),name varchar(255),addr varchar(255)).
 Now we will focus on report itself (Example1Report class).
 
* The pdfFactory variable represent the actual render - the previous version had ITextFactory.
 Now since the IText was removed the only factory is PdfNativeFactory.
 Sometime in the future I will add another for HTML and maybe for docx.
* The report has a static method create that return the actual Report object.
* Before the report we need to know the header and footer size.Note that all sizes are in pdf units(1 ⁄ 72 inch).
* Actual Java8 closure takes pagenumber as parameter-in the example we don't have any header for the first page .
* We are using groups in reports.The class Group parameters are the group's name 
 and the closure that take a record and return the record's group - in our case is just
 the groupName field , but can be any Java expression.
* We can have multiple groups and the group order is the order in which they appear in
 GroupUtil.apply.Note that the data should be sorted in the same order- report doesn't do
 any sorting of data.
* The order of header/footers:
* Header for group 1
    * Header for group 2
        * Header for group 3                
          Some code
        * Footer for group 3
    * Footer for group 2
* Footer for group 1

* Let's put data in a tabular format in two columns both left allign.
We need to define a row starting at 10. to report page's with -10 and has two column each of 150 points.

    Row row = Row.apply(10.f, report.pgSize().width() - 10, Column.apply("name", 150f),Column.apply("addr", 150f));
    
    For the text we need a RCell with the text value ,color and font attribute 
    and allign's type:
    
    RCell h_name = new RCell(new RText("Name").bold().color(headerFontColor)).leftAllign().between(m_name);
    
    We need to assemble both cells together:RRow hrow = RRow.apply(h_name, h_addr);
    
    You can use  hrow.calculate(report) if you need the new line position.
    
    Let's draw a round corner rectangle before putting the header:
    
    report.rectangle().from(9, report.getY() - report.lineHeight()).radius(3).to(report.pgSize().width() - 9, y2 + 2).fillColor(headerColor).draw();

    Now print the row with columns:hrow.print(report);.
    
    At the end report.render(); creates the pdf file and close the report.
    
    
    
    
                
 
 
