import sbt.Keys.{libraryDependencies, publishMavenStyle}

val SCALA_VERSION = "2.13.2"

val AKKA_VERSION = "2.6.4" //latest.release"

val ROCKSDB_VERSION = "6.7.3" //latest.release"

val FASTERXML = "2.10.3" //latest.release"

val projectVersion = "1.0.8-SNAPSHOT"

lazy val commonInclude = Seq(
	organization := "com.github.sysalto",
	isSnapshot := true,
	version := projectVersion,
	cancelable in Global := true,
	scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature"),
	Keys.fork in run := true,
	resolvers += Resolver.sonatypeRepo("snapshots"),
//	resolvers += "Akka Snapshot Repository" at "http://repo.akka.io/snapshots/",

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

	pomExtra :=
		<url>https://github.com/sysalto/ReactiveReports</url>
			<licenses>
				<license>
					<name>GNU Lesser General Public License version 3</name>
					<url>https://www.gnu.org/licenses/lgpl-3.0.en.html</url>
					<distribution>repo</distribution>
				</license>
			</licenses>
			<scm>
				<url>https://github.com/sysalto/ReactiveReports.git</url>
				<connection>scm:git:git@github.com:sysalto/ReactiveReports.git</connection>
			</scm>
			<developers>
				<developer>
					<id>marian.mihai</id>
					<name>Marian Mihai</name>
				</developer>
			</developers>

)

lazy val commonSettings = Seq(
	scalaVersion := SCALA_VERSION,
)

lazy val coreSettings = Seq(
	crossScalaVersions := Seq("2.11.12","2.12.8", SCALA_VERSION),
	javacOptions ++= {
		if (scalaVersion.value != "2.11.12" ) Seq("-source", "1.8", "-target", "1.8") else Seq("-source", "1.6", "-target", "1.6")
	},
	libraryDependencies += "org.rocksdb" % "rocksdbjni" % ROCKSDB_VERSION,
	libraryDependencies += "com.fasterxml.jackson.module" %% "jackson-module-scala" % FASTERXML,
	libraryDependencies += "com.fasterxml.jackson.dataformat" % "jackson-dataformat-cbor" % FASTERXML
)

lazy val renderPdfSettings = Seq(
	crossScalaVersions := Seq("2.11.12","2.12.8", SCALA_VERSION),
	javacOptions ++= {
		if (scalaVersion.value != "2.11.12" ) Seq("-source", "1.8", "-target", "1.8") else Seq("-source", "1.6", "-target", "1.6")
	}
)

lazy val akkaSettings = Seq(
	crossScalaVersions := Seq("2.12.8", SCALA_VERSION),
	javacOptions ++= {
		Seq("-source", "1.8", "-target", "1.8")
	},
	libraryDependencies += "com.typesafe.akka" %% "akka-actor" % AKKA_VERSION,
	libraryDependencies += "com.typesafe.akka" %% "akka-slf4j" % AKKA_VERSION,
	libraryDependencies += "com.typesafe.akka" %% "akka-stream" % AKKA_VERSION
)


lazy val reactiveReports = (project in file(".")).settings(commonInclude: _*).
	settings(commonSettings: _*).
	enablePlugins(JavaAppPackaging) dependsOn(
	coreReport, coreReportAkka, renderPdf, examples)

lazy val coreReport = (project in file("core/report")).settings(commonInclude: _*).
	settings(name := "ReactiveReports Core").settings(commonSettings: _*)
	.settings(coreSettings: _*)
	.enablePlugins(JavaAppPackaging)


lazy val coreReportAkka = (project in file("core/reportAkka")).settings(commonInclude: _*).
	settings(name := "ReactiveReports Core Akka").settings(commonSettings: _*).
	settings(akkaSettings: _*).enablePlugins(JavaAppPackaging) dependsOn coreReport


lazy val renderPdf = (project in file("core/renders/pdf")).settings(commonInclude: _*).
	settings(name := "ReactiveReports Pdf Render").settings(commonSettings: _*).
	settings(renderPdfSettings: _*).enablePlugins(JavaAppPackaging) dependsOn coreReport



lazy val exampleSettings = Seq(
	name := "Reports Examples",
	organization := "com.github.sysalto",
	version := projectVersion,
	cancelable in Global := true,
	scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature"),
	scalaVersion := SCALA_VERSION,
	Keys.fork in run := true,
	resolvers += Resolver.sonatypeRepo("public"),
//	resolvers += "Akka Snapshot Repository" at "http://repo.akka.io/snapshots/",
	resolvers += Resolver.mavenLocal,
	libraryDependencies += ("org.scala-lang.modules" %% "scala-xml" % "latest.release"),
	libraryDependencies += ("org.hsqldb" % "hsqldb" % "latest.release"),
	libraryDependencies += ("org.apache.derby" % "derby" % "latest.release"),
	libraryDependencies += "com.typesafe.akka" %% "akka-http" % "latest.release"
)

lazy val examples = (project in file("examples")).
	settings(exampleSettings: _*).
	settings(
		name := "ReactiveReports Examples",
		libraryDependencies += "com.typesafe.akka" %% "akka-http" % "latest.release",
		libraryDependencies += ("org.scala-lang.modules" %% "scala-xml" % "latest.release"),
		libraryDependencies += ("org.hsqldb" % "hsqldb" % "latest.release"),
		libraryDependencies += "org.slf4j" % "slf4j-log4j12" % "1.7.25"
	).enablePlugins(JavaAppPackaging) dependsOn(coreReport, coreReportAkka, renderPdf)

