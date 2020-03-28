import com.typesafe.sbt.packager.docker.ExecCmd
scalaVersion := "2.12.8"
name := "bully-leader-election"
enablePlugins(JavaAppPackaging)
enablePlugins(DockerPlugin)

mainClass in Compile := Some("io.parapet.examples.bully.App")

mainClass in (Compile, packageBin) := Some("io.parapet.examples.bully.App")
mainClass in (Compile, run) := Some("io.parapet.examples.bully.App")
mainClass in assembly := Some("io.parapet.examples.bully.App")
assemblyJarName in assembly := "app.jar"


daemonUserUid in Docker := None
daemonUser in Docker := "root"
dockerCommands ++= Seq(
  ExecCmd("RUN",
    "apt-get", "update"
  ),
  ExecCmd("RUN",
    "apt-get", "install", "nano"
  )
)