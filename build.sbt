val SCALA_VERSION = "2.12.1"

val AKKA_VERSION="2.4.16"

val TYPESAFE_CONFIG="1.3.1"

val LOGBACK_VERSION="1.1.3"

val ROCKSDB_VERSION="5.0.1"

val KRYO_VERSION="0.5.2"

val ITEXT_VERSION="5.5.10"

val JFREECHART_VERSION="1.0.19"

lazy val commonInclude = Seq(
  organization := "com.sysalto",
  version := "1.0.0-alpha.1",
  cancelable in Global := true,
  scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature"),
  incOptions := incOptions.value.withNameHashing(true),
  Keys.fork in run := true,
  resolvers += Resolver.sonatypeRepo("snapshots"),
  resolvers += "Akka Snapshot Repository" at "http://repo.akka.io/snapshots/"
)


lazy val commonSettings = Seq(
  scalaVersion := SCALA_VERSION,
  libraryDependencies += "com.typesafe.akka" %% "akka-actor" % AKKA_VERSION,
  libraryDependencies += "com.typesafe.akka" %% "akka-slf4j" % AKKA_VERSION,
  libraryDependencies += "com.typesafe.akka" %% "akka-stream" % AKKA_VERSION,
  libraryDependencies += "com.typesafe" % "config" % TYPESAFE_CONFIG,
  libraryDependencies += "ch.qos.logback" % "logback-classic" % LOGBACK_VERSION,
  libraryDependencies += "org.rocksdb" % "rocksdbjni" % ROCKSDB_VERSION,
  libraryDependencies += "com.github.romix.akka" %% "akka-kryo-serialization" % KRYO_VERSION
)


lazy val renderItextSettings = Seq(
  scalaVersion := SCALA_VERSION,
  libraryDependencies += "com.itextpdf" % "itextpdf" % ITEXT_VERSION,
  libraryDependencies += "org.jfree" % "jfreechart" % JFREECHART_VERSION
)


lazy val reactiveReports = (project in file(".")).settings(commonInclude: _*).
  settings(commonSettings: _*).enablePlugins(JavaAppPackaging) dependsOn(coreReport, renderItext,examples)

lazy val coreReport = (project in file("core/report")).settings(commonInclude: _*).
  settings(name := "ReactiveReports Core").
  settings(commonSettings: _*).enablePlugins(JavaAppPackaging)

lazy val renderItext = (project in file("core/renders/itext")).settings(commonInclude: _*).
  settings(name := "ReactiveReports Itext Render").
  settings(renderItextSettings: _*).enablePlugins(JavaAppPackaging) dependsOn coreReport

lazy val examples = (project in file("examples")).
  settings(commonSettings: _*).
  settings(
    name := "ReactiveReports Examples",
    libraryDependencies += "com.typesafe.akka" %% "akka-http" % "latest.release",
    libraryDependencies += ("org.scala-lang.modules" %% "scala-xml" % "latest.release"),
    libraryDependencies += ("org.hsqldb" % "hsqldb" % "latest.release")
  ).enablePlugins(JavaAppPackaging) dependsOn(coreReport, renderItext)

