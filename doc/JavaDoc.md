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
 
 * the pdfFactory variable represent the actual render - the previous version had ITextFactory.
 Now since the IText was removed the only factory is PdfNativeFactory.
 Sometime in the future I will add another for HTML and maybe for docx.
 * The report has a static method create that return the actual Report object.
 * Before the report we need to know the header and footer size.Note that all sizes are in pdf units(1 ⁄ 72 inch).
 * Actual Java8 closure takes pagenumber as parameter-in the example we don't have any haeder for the fisrt page 0.
 * 
  
 
 