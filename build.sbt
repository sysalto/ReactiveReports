import sbt.Keys.publishMavenStyle

val SCALA_VERSION = "2.12.4"

val AKKA_VERSION = "latest.release" // "2.5.9"

val ROCKSDB_VERSION = "latest.release" // "5.9.2"

val PROTOBUF_VERSION = "latest.release" // "3.5.1"

//val projectVersion = "1.0.0-RC.5.3"
val projectVersion = "1.0.0-SNAPSHOT"

lazy val commonInclude = Seq(
	organization := "com.github.sysalto",
	isSnapshot := true,
	version := projectVersion,
	cancelable in Global := true,
	scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature"),
	//incOptions := incOptions.value.withNameHashing(true),
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
	crossScalaVersions := Seq("2.11.12", SCALA_VERSION),
	javacOptions ++= {
		if (scalaVersion.value == SCALA_VERSION) Seq("-source", "1.8", "-target", "1.8") else Seq("-source", "1.6", "-target", "1.6")
	},
	libraryDependencies += "org.rocksdb" % "rocksdbjni" % ROCKSDB_VERSION,
	libraryDependencies += "com.google.protobuf" % "protobuf-java" % PROTOBUF_VERSION
)

lazy val renderPdfSettings = Seq(
	crossScalaVersions := Seq("2.11.12", SCALA_VERSION),
	javacOptions ++= {
		if (scalaVersion.value == SCALA_VERSION) Seq("-source", "1.8", "-target", "1.8") else Seq("-source", "1.6", "-target", "1.6")
	}
)

lazy val akkaSettings = Seq(
	libraryDependencies += "com.typesafe.akka" %% "akka-actor" % AKKA_VERSION,
	libraryDependencies += "com.typesafe.akka" %% "akka-slf4j" % AKKA_VERSION,
	libraryDependencies += "com.typesafe.akka" %% "akka-stream" % AKKA_VERSION
)


lazy val reactiveReports = (project in file(".")).settings(commonInclude: _*).
	settings(commonSettings: _*).
	enablePlugins(JavaAppPackaging) dependsOn(
	coreReport, coreReportAkka, renderPdf, examples)

lazy val coreReport = (project in file("core/report")).settings(commonInclude: _*).
	settings(name := "ReactiveReports Core").settings(commonSettings: _*).
	settings(coreSettings: _*).enablePlugins(JavaAppPackaging)


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
	//  incOptions := incOptions.value.withNameHashing(true),
	Keys.fork in run := true,
	resolvers += Resolver.sonatypeRepo("public"),
	resolvers += "Akka Snapshot Repository" at "http://repo.akka.io/snapshots/",
	resolvers += Resolver.mavenLocal,
	libraryDependencies += ("org.scala-lang.modules" %% "scala-xml" % "latest.release"),
	libraryDependencies += ("org.hsqldb" % "hsqldb" % "latest.release"),
	libraryDependencies += "com.typesafe.akka" %% "akka-http" % "latest.release"
)

lazy val examples = (project in file("examples")).
	settings(exampleSettings: _*).
	settings(
		name := "ReactiveReports Examples",
		libraryDependencies += "com.typesafe.akka" %% "akka-http" % "latest.release",
		libraryDependencies += ("org.scala-lang.modules" %% "scala-xml" % "latest.release"),
		libraryDependencies += ("org.hsqldb" % "hsqldb" % "latest.release"),
		libraryDependencies += "com.danielasfregola" %% "twitter4s" % "latest.release"
	).enablePlugins(JavaAppPackaging) dependsOn(coreReport, coreReportAkka, renderPdf)

