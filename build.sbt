name := "examples"
organization := "io.parapet"
version := "1.0.0-SNAPSHOT"
scalaVersion := "2.12.8"

lazy val global = project
  .in(file("."))
  .aggregate(
    bullyLeaderElection
  )

lazy val bullyLeaderElection = project
  .in(file("bully-leader-election"))
  .settings(
    name := "bully-leader-election",
    libraryDependencies += "io.parapet" %% "core" % "0.0.1-RC3",
    libraryDependencies += "io.parapet" %% "interop-cats" % "0.0.1-RC3",
    libraryDependencies += "io.parapet" % "p2p" % "1.0.0-SNAPSHOT",
    libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3",
    libraryDependencies += "net.logstash.logback" % "logstash-logback-encoder" % "5.3"
  )
