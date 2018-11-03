logLevel := Level.Warn
addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "latest.release")
addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "latest.release")
addSbtPlugin("com.jsuereth" % "sbt-pgp" % "latest.release")
addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "latest.release")
addSbtPlugin("com.github.gseitz" % "sbt-protobuf" % "latest.release")