#Reactive Reports

Reactive Reports is a framework developed in Scala, designed for generating reports. 

Combining powerful Scala language with the report DSL you can create amazing documents.

It is using the iText pdf library (http://itextpdf.com/) and JFreeChart (http://www.jfree.org/jfreechart/).

Please see LICENSE.md and ItextLICENSE.md.

It is powered by Akka, and it enables the creation of multiple reports from streams.
It can process huge amount of data without increasing the memory by rendering it one record at a time. 

It consists of two jars:
* reactive-reports-core - the main core report framework that is render agnostic
* reactive-reports-itext-render - itext render for reactive reports

In this first version, ReactiveReports can be used in Scala 2.12. A Java version will follow soon.

### Sbt installation:

  libraryDependencies += "com.github.sysalto" %% "reactivereports-core" % "1.0.0-alpha.1",
  
  libraryDependencies += "com.github.sysalto" %% "reactivereports-itext-render" % "1.0.0-alpha.1".
  
  
### Getting started
   
* Create an sbt build file to include reactive-reports jars and any other additional dependencies.
* Copy the HelloWorldReport Scala object from com.sysalto.report.examples into your source folder.
* Run this file.

We provide a helper trait ReportApp for simpler reports. Ths Scala object extends this helper trait and 
imports Implicits.

The input to the reports can be a list of any Java/Scala objects, of type java.sql.resultset or webservice results. 
Please see com.sysalto.report.examples.mutualFunds.MutualFundsReport and com.sysalto.report.examples.rss.RssReport.
The output of  MutualFundsReport is MutualFunds.pdf.
 
It has a simple DSL for printing text of variable sizes and colors and enables column wrapping.
 
If you need to keep one group data on the same page, please see com.sysalto.report.examples.groups.Report2 - report.
cut and report.paste.
  
It can create multiple reports based on common data, selecting the database records only once and creating different 
reports simultaneously.
Please see com.sysalto.report.examples.rss.RssReport.
  
