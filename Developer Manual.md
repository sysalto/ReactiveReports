## Reactive Reports Developer Manual

Reactive Reports is a framework written in Scala with Java port , so it can be easily used from anywhere where Java is used.

Minimum Jdk supported by library is JDK1.6 , but without akka support.
To use with Jdk1.6 , you have to use maven dependencies:
* reactivereports-core_2.11
* reactivereports-pdf-render_2.11

For using with JDK >=8 or scala , please use :
* reactivereports-core_2.12
* reactivereports-pdf-render_2.12

For using with akka (needs JDK>=8) :
* reactivereports-core-akka_2.12



This framework support any kind of input (akka streams, Java/Scala array of objects , database resultsets).
If the input is an webservice , please use akka for best results.
For the same input you can generate multiple reports.\
The report is written only in code giving a great flexibility.
Internally , the framework used a paginated memory (by default provided by RocksDb , but can by easly setup 
in constructor for any other provider).It means that it can process any number of input size 
(the limit is however the HDD free where the memory is paginated). It have tested with tens of millions 
of records with very low memory footprint. 

The framework has an open plugin architecture- first we implemented with Itext , but we dropped because of the 
license limitation.Then  we build a pdf render from scratch.\
We plan to add in the future a HTML render plugin.


#Java using

The common way to use it is to create a maven project and add the folowing dependencies :
* reactivereports-core_2.11
* reactivereports-pdf-render_2.11\
or 2.12 for JDK>=8.




#Scala using
  




