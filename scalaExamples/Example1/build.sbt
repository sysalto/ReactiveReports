
name := "Example1"

version := "1.0"

scalaVersion := "2.12.2"

lazy val exampleSettings = Seq(
	name := "Reports Examples",
	organization := "com.github.sysalto",
	version := "1.0.0",
	scalaVersion := "2.12.2",
	cancelable in Global := true,
	scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature"),
	incOptions := incOptions.value.withNameHashing(true),
	Keys.fork in run := true,
	resolvers += Resolver.sonatypeRepo("public"),
	resolvers += "Akka Snapshot Repository" at "http://repo.akka.io/snapshots/",
	resolvers += Resolver.mavenLocal,
	libraryDependencies += ("org.hsqldb" % "hsqldb" % "latest.release"),
	libraryDependencies += "com.github.sysalto" %% "reactivereports-core" % "1.0.0-beta.1",
	libraryDependencies += "com.github.sysalto" %% "reactivereports-core-akka" % "1.0.0-beta.1",
	libraryDependencies += "com.github.sysalto" %% "reactivereports-pdf-render" % "1.0.0-beta.1"
)

lazy val examples = (project in file(".")).
	settings(exampleSettings: _*)