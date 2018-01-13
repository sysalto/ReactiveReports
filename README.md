# Reactive Reports

 - Reactive Reports is an Open Source, scalable framework developed in Scala, designed for generating reports from code.
 - It uses the Akka framework (https://akka.io) for handling data streams.
 - It can be automated from a shell script or tool, providing maximum flexibility, in terms of automation tools. 
 - Reports can be generated in Scala or Java. Both in Java and Scala, you can generate the reports with or without Akka.  For use with Akka, you have to include `reactivereports-core-akka`.
 - It provides the ability to generate one or mutiple reports at once, in parallel, from the same input, or from multiple inputs. In doing this, it selects the database records only once and creates different reports simultaneously.
 Please see `com.sysalto.report.examples.rss.RssReport` as an example.
 - Its architecture is designed to conserve memory, and users will never get out of memory errors for large amounts of data
 - Reactive Reports has the full control and responsibility over the PDF generation capability

The input to the framework can be:
  - one or more data streams - only for Akka
  - database records - here, the client application connects to a database through a JDBC connection
  - collection of Java or Scala objects
  
The output for the report is done in PDF.

In Java, you would create the reports using Java code.
In Scala, we have provided a DSL - domain specific language - to create the report. This DSL is very simple to use. We will update the Wiki with more explanations on that. This DSL allows printing text of different sizes and colors and enables column wrapping.

The license is LGPL. Please see LICENSE.md. According to this license, **Reactive Reports is free to use and modify, for both Open Source and commercial uses.** 

ReactiveReports can be used in Scala 2.12 and in Java 8.

As running examples, please see `com.sysalto.report.examples.mutualFunds.MutualFundsReport` and `com.sysalto.report.examples.rss.RssReport`.
The output of  MutualFundsReport is MutualFunds.pdf.

  
## Getting started
   
### 1 - Cloning the git repo and running the examples
```
git clone https://github.com/sysalto/ReactiveReports
sbt update
sbt compile
sbt
projects
project examples
run
Select the number corresponding to the project you want to run
Check the PDF file newly generated, found in the examples folder
```

### 2 - For Java, using the maven artifact
* For scala clone the Example1 sbt project. You can get in from scalaExamples.
* For java clone the Example2 maven project for standalone java project or Example1 for web based java project.
You can get these projects from javaExamples. 

We provide a helper trait ReportApp for simpler reports. Ths Scala object extends this helper trait and 
imports Implicits.
 
If you need to keep one group data on the same page, please see `com.sysalto.report.examples.groups.Report2` - `report.
cut` and `report.paste`.






  
