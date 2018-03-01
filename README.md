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
In Scala, we have provided a DSL - domain specific language - to create the report. This DSL is very simple to use. We will update the documentation with more explanations on that. This DSL allows printing text of different sizes and colors and enables column wrapping.

The license is LGPL. Please see LICENSE.md. According to this license, **Reactive Reports is free to use and modify, for both Open Source and commercial uses.** 

ReactiveReports can be used in Scala 2.12 and in Java 7 and 8.

As running examples, please see `com.sysalto.report.examples.mutualFunds.MutualFundsReport` and `com.sysalto.report.examples.rss.RssReport`.
The output of  MutualFundsReport is MutualFunds.pdf.

  
## Getting started
   
### 1 - Using SBT - Recommended for Scala
Clone the git repo and run the examples
For running the Scala examples, we recommend cloning the git repo and running the examples, like shown below.
```
1 - git clone https://github.com/sysalto/ReactiveReports
2 - sbt update
3 - sbt compile
4 - sbt
5 - projects
6 - project examples
7 - run
8 - Select the number corresponding to the project you want to run
9 - Check the PDF file newly generated, found in the examples folder
```

### 2 - Using Maven - Recommeded for Java
For running the Java examples, we recommend using Maven, not sbt. Although sbt can be used for Java, as well, adding Maven dependencies is a better practice. 

Under `javaExamples`, we provide 3 examples: Example 1, Example 2 and Example 3. 

**Step 1** - Download Intellij Idea Community Edition from here - https://www.jetbrains.com/idea/download/#section=mac

**Step 2** - Open the Idea editor, and create a Maven artifact, by navigating to `File -> New -> Project -> Maven`

**Step 3** - Use the latest Maven artifacts for Reactive Reports.

**Step 4** - Go to `src/main/java`, and create a class. Paste the code from one of the Java examples in `javaExample/Example3`. This will generate a PDF report in your project, under the root directory. 



### Other Open Source products used within Reactive Reports

**Open Source dependencies used by Reactive Reports framework**

**1 - Akka** - https://akka.io/

**2 - RocksDB** - http://rocksdb.org/

**3 - Protobuf3** from Google - https://developers.google.com/protocol-buffers/docs/proto3

**Open Source dependencies only used in examples, not in the framework**

**1 - HyperSQL** - http://hsqldb.org/

**2 - Twitter4s** - https://github.com/DanielaSfregola/twitter4s






  
