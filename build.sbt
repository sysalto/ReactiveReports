import sbt.Keys.publishMavenStyle

val SCALA_VERSION = "2.12.2"

val AKKA_VERSION = "latest.release"

val TYPESAFE_CONFIG = "latest.release"

val LOGBACK_VERSION = "latest.release"

val ROCKSDB_VERSION = "latest.release"

val KRYO_VERSION = "latest.release"

val ITEXT_VERSION = "latest.release"

val JFREECHART_VERSION = "latest.release"

val projectVersion = "1.0.0-alpha.3"

lazy val commonInclude = Seq(
  organization := "com.github.sysalto",
  isSnapshot := false,
  version := projectVersion,
  cancelable in Global := true,
  scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature"),
  incOptions := incOptions.value.withNameHashing(true),
  Keys.fork in run := true,
  resolvers += Resolver.sonatypeRepo("snapshots"),
  resolvers += "Akka Snapshot Repository" at "http://repo.akka.io/snapshots/",

  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (isSnapshot.value) {
      Some("snapshots" at nexus + "content/repositories/snapshots")
    }
    else {
      Some("releases" at nexus + "service/local/staging/deploy/maven2")
    }
  },
  publishMavenStyle := true,
  pomIncludeRepository := { _ => false },

  pomExtra := (
    <url>https://github.com/sysalto/ReactiveReports</url>
      <licenses>
        <license>
          <name>GNU Affero General Public License version 3</name>
          <url>https://www.gnu.org/licenses/agpl-3.0.en.html.</url>
          <distribution>repo</distribution>
        </license>
      </licenses>
      <scm>
        <url>https://github.com/sysalto/ReactiveReports.git</url>
        <connection>scm:git:git@github.com:sysalto/ReactiveReports.git</connection>
      </scm>
      <developers>
        <developer>
          <id>marian.nedelescu</id>
          <name>Marian Nedelescu</name>
        </developer>
      </developers>)

)


lazy val commonSettings = Seq(
  scalaVersion := SCALA_VERSION,
  libraryDependencies += "com.typesafe" % "config" % TYPESAFE_CONFIG,
  libraryDependencies += "ch.qos.logback" % "logback-classic" % LOGBACK_VERSION,
  libraryDependencies += "org.rocksdb" % "rocksdbjni" % ROCKSDB_VERSION,
  libraryDependencies += "com.github.romix.akka" %% "akka-kryo-serialization" % KRYO_VERSION
)

lazy val akkaSettings = Seq(
  scalaVersion := SCALA_VERSION,
  libraryDependencies += "com.typesafe.akka" %% "akka-actor" % AKKA_VERSION,
  libraryDependencies += "com.typesafe.akka" %% "akka-slf4j" % AKKA_VERSION,
  libraryDependencies += "com.typesafe.akka" %% "akka-stream" % AKKA_VERSION
)

lazy val renderItextSettings = Seq(
  scalaVersion := SCALA_VERSION,
  libraryDependencies += "com.itextpdf" % "itextpdf" % ITEXT_VERSION,
  libraryDependencies += "org.jfree" % "jfreechart" % JFREECHART_VERSION
)


lazy val reactiveReports = (project in file(".")).settings(commonInclude: _*).
  settings(commonSettings: _*).enablePlugins(JavaAppPackaging) dependsOn(
    coreReport, coreReportAkka, renderItext, renderPdf, examples)

lazy val coreReport = (project in file("core/report")).settings(commonInclude: _*).
  settings(name := "ReactiveReports Core").
  settings(commonSettings: _*).enablePlugins(JavaAppPackaging)


lazy val coreReportAkka = (project in file("core/reportAkka")).settings(commonInclude: _*).
  settings(name := "ReactiveReports Core Akka").
  settings(commonSettings: _*).enablePlugins(JavaAppPackaging) dependsOn coreReport

lazy val renderItext = (project in file("core/renders/itext")).settings(commonInclude: _*).
  settings(name := "ReactiveReports Itext Render").
  settings(renderItextSettings: _*).enablePlugins(JavaAppPackaging) dependsOn coreReport

lazy val renderPdf = (project in file("core/renders/pdf")).settings(commonInclude: _*).
  settings(name := "ReactiveReports Pdf Render").
  settings(renderItextSettings: _*).enablePlugins(JavaAppPackaging) dependsOn coreReport


lazy val exampleSettings = Seq(
  name := "Reports Examples",
  organization := "com.github.sysalto",
  version := projectVersion,
  cancelable in Global := true,
  scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature"),
  scalaVersion := "2.12.1",
  incOptions := incOptions.value.withNameHashing(true),
  Keys.fork in run := true,
  resolvers += Resolver.sonatypeRepo("public"),
  resolvers += "Akka Snapshot Repository" at "http://repo.akka.io/snapshots/",
  resolvers += Resolver.mavenLocal,
  libraryDependencies += ("org.scala-lang.modules" %% "scala-xml" % "latest.release"),
  libraryDependencies += ("org.hsqldb" % "hsqldb" % "latest.release"),
  libraryDependencies += "com.typesafe.akka" %% "akka-http" % "latest.release"
  //  libraryDependencies += "com.github.sysalto" %% "reactivereports-core" % projectVersion,
  //  libraryDependencies += "com.github.sysalto" %% "reactivereports-itext-render" % projectVersion

)

lazy val examples = (project in file("examples")).
  settings(exampleSettings: _*).
  settings(
    name := "ReactiveReports Examples",
    libraryDependencies += "com.typesafe.akka" %% "akka-http" % "latest.release",
    libraryDependencies += ("org.scala-lang.modules" %% "scala-xml" % "latest.release"),
    libraryDependencies += ("org.hsqldb" % "hsqldb" % "latest.release")
  ).enablePlugins(JavaAppPackaging) dependsOn(coreReport,coreReportAkka, renderItext,renderPdf)

