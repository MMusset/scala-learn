
import sbt.Keys.scalacOptions

val CirceVersion           = "0.14.1"
val EnumeratumVersion      = "1.7.0"
val ScalaCheckVersion      = "1.15.4"
val ScalaCheckFakerVersion = "7.0.0"
val ScalaTestVersion       = "3.2.10"
val ScalaTestPlusVersion   = "3.1.0.0-RC2"
val SttpClientVersion      = "3.3.18"
val Log4CatsVersion            = "1.1.1"

lazy val dependencies         = Seq(
  "com.beachape"                  %% "enumeratum"               % EnumeratumVersion   ,
  "com.softwaremill.sttp.client3" %% "armeria-backend-cats-ce2" % SttpClientVersion   ,
  "com.softwaremill.sttp.client3" %% "circe"                    % SttpClientVersion   ,
  "com.softwaremill.sttp.client3" %% "core"                     % SttpClientVersion   ,
  "ch.qos.logback"                 % "logback-classic"          % "1.2.5"             ,
  "com.typesafe"                   % "config"                   % "1.4.1"             ,
  "io.circe"                      %% "circe-generic"            % CirceVersion        ,
  "io.circe"                      %% "circe-generic-extras"     % CirceVersion        ,
  "io.circe"                      %% "circe-literal"            % CirceVersion        ,
  "io.circe"                      %% "circe-parser"             % CirceVersion        ,
  "io.chrisdavenport"             %% "log4cats-slf4j"           % Log4CatsVersion     ,
  "org.scalacheck"                %% "scalacheck"               % ScalaCheckVersion
)

lazy val root                 = (project in file("."))
  .settings(
    name                          := "scalalearn",
    scalaVersion                  := "2.13.7",
    version                       := "0.0.1-SNAPSHOT",
    fork                          := true,
    Global / cancelable           := true,
    Global / onChangedBuildSource := ReloadOnSourceChanges,
    ThisBuild / closeClassLoaders := false,
    libraryDependencies ++= dependencies,
    addCompilerPlugin("org.typelevel" % "kind-projector"     % "0.13.2" cross CrossVersion.full),
    addCompilerPlugin("com.olegpy"   %% "better-monadic-for" % "0.3.1"),
    scalacOptions ++= Seq(
      "-unchecked",
      "-deprecation",
      "-feature",
      "-Ywarn-unused:imports",
      "-Xfatal-warnings",
      "-language:postfixOps",
      "-language:higherKinds",
      "-language:implicitConversions"
    ),
    Test / javaOptions += "-Duser.timezone=UTC"
  )
