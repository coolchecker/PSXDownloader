lazy val root = (project in file("."))
  .enablePlugins(JavaServerAppPackaging)
  .settings(
    organization := "lizheng",
    name := "psxdownloader",
    version := "1.0",
    scalaVersion := "2.11.8",
    mainClass in Compile := Some("lizheng.psxdownloader.Bootstrap"),
    libraryDependencies ++= Seq(
      "ch.qos.logback" % "logback-classic" % "1.1.7"
    )
  )