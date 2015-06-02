lazy val projectSettings = Seq(
  name := "ConsulHttpClient",
  version := "1.0.0",
  organization := "tritondigital"
)

lazy val scalaSettings = Seq(
  scalaVersion := "2.11.6",
  crossScalaVersions := Seq("2.10.4", "2.11.6")
)

lazy val librairies = Seq(
  "com.ning" % "async-http-client" % "1.9.24",
  "org.json4s" %% "json4s-native" % "3.2.10",
  "com.typesafe.akka" %% "akka-actor" % "2.3.11",
  "com.typesafe.akka" %% "akka-testkit" % "2.3.11" % Test,
  ("org.scalatest" %% "scalatest" % "2.2.4" % Test).exclude("com.typesafe.akka", "akka-actor_2.11"),
  "spray-gun" % "spray-gun_2.11" % "1.2.0" % Test
)
lazy val scoverageSettings = Seq(
  ScoverageSbtPlugin.ScoverageKeys.coverageMinimum := 95,
  ScoverageSbtPlugin.ScoverageKeys.coverageFailOnMinimum := true
)

lazy val ConsulHttpClient = (project in file("."))
  .settings(projectSettings: _*)
  .settings(scalaSettings: _*)
  .settings(libraryDependencies ++= librairies)
  .settings(scoverageSettings: _*)
  .settings(resolvers += JCenterRepository)
  .settings(testOptions in Test += Tests.Argument(TestFrameworks.ScalaTest, "-F", sys.props.getOrElse("scaleFactor", default = "1.0")))