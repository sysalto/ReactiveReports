# Reactive Reports

Reactive Reports is a framework developed in Scala, designed for generating reports. 

Combining powerful Scala language with the report DSL you can create amazing documents.

License type is LGPL. Please see LICENSE.md.

It is powered by Akka, and it enables the creation of multiple reports from streams.
It can process huge amount of data without increasing the memory by rendering it one record at a time. 

ReactiveReports can be used in Scala 2.12 and in Java 8.

  
### Getting started
   
* For scala clone the Example1 sbt project. You can get in from scalaExamples.
* For java clone the Example2 maven project for standalone java project or Example1 for web based java project.
You can get these projects from javaExamples. 

This framework can be used with or without Akka in Java or Scala.
For use with Akka you have to include reactivereports-core-akka.

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
Please see `com.sysalto.report.examples.rss.RssReport`.



  
