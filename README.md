# Reactive Reports

### Unique features 
 - Scalable framework developed in Scala, designed for generating reports from code.
 - Support of Scala, Java, other JVM languages (i.e. Groovy) 
 - Pluggable architecture
 - Open Source - LGPL license - **free to use and modify, for both Open Source and commercial uses** 
 
 <img width="1012" alt="diagram_rr" src="https://user-images.githubusercontent.com/25547970/47624281-27880800-daf1-11e8-98d0-f4b4fa7284c4.png">
 
 ### Fit for Big data
 - Strong product for processing big data – the hard disk size is the limit
 - Designed to **conserve memory;** report memory data is stored on the disk, in RocksDB.
 - Built in support for Akka streams input
 
### Flexibility
 - Can be integrated into CI/CD pipelines (i.e., Jenkins)
 - Reactive Reports applications can run on any JVM runtime
 - Reactive Reports applications can be Dockerized
 - Applications can integrate with other libraries, such as AI/ML, NLP and others.

### Speed
 - Uses scalaz memoizing for speeding calculations
 - Generated a 20,000-page report with images in 4 minutes
 
### Graphics
 - Built in support for French and Spanish character set 
 - Can build graphics using the PDF standard (tables, color gradients, pie charts, bar charts, etc.)
 - Can include external images (ie: JPG, etc)
 - Can create in document or external links, and table of contents at beginning or end of reports
 - Reactive Reports has the full control and responsibility over the PDF generation capability

### Language details
 - In Java, reports are created using Java code.
 - In Scala, Reactive Reports provides a DSL - domain specific language - for report creation. This DSL is simple to use. 
 - Reactive Reports can be used in Scala 2.13, 2.12 , 2.11 and in Java 7 and 8.

  
## Getting started
   
### 1 - Using SBT - Recommended for Scala
Include the following in your `build.sbt` file:

```
libraryDependencies += "com.github.sysalto" %% "reactivereports-core" % "1.0.2"
libraryDependencies += "com.github.sysalto" %% "reactivereports-pdf-render" % "1.0.2"
```

### 2 - Using Maven - Recommeded for Java

Use the latest Maven artifacts for Reactive Reports. Example:

```
<dependency>
  <groupId>com.github.sysalto</groupId>
  <artifactId>reactivereports-core_2.12</artifactId>
  <version>1.0.2</version> 
</dependency>

<dependency>
  <groupId>com.github.sysalto</groupId>
  <artifactId>reactivereports-pdf-render_2.12</artifactId>
  <version>1.0.2</version>
</dependency>
```

### Built using

**Open Source dependencies used by Reactive Reports framework**

**1 - Scala**

**2 - Akka** - https://akka.io/

**3 - RocksDB** - http://rocksdb.org/

**4 - Protobuf** - https://developers.google.com/protocol-buffers/docs/proto3

**5 - Scalaz** - https://github.com/scalaz/scalaz

**Open Source dependencies only used in examples, not in the framework**

**1 - HyperSQL** - http://hsqldb.org/

**2 - Twitter4s** - https://github.com/DanielaSfregola/twitter4s

### Contact

For questions, concerns, or simply to get in touch with us, please, email us at `sysaltocorporation@gmail.com`
