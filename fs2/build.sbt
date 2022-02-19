import sbt.Keys.scalacOptions

val CirceVersion           = "0.14.1"
val EnumeratumVersion      = "1.7.0"
val Http4sVersion          = "0.22.8"
val ScalaCheckVersion      = "1.15.4"
val ScalaCheckFakerVersion = "7.0.0"
val ScalaTestVersion       = "3.2.10"
val ScalaTestPlusVersion   = "3.1.0.0-RC2"
val SttpClientVersion      = "3.3.18"
val Log4CatsVersion        = "1.1.1"
val ConfigVersion          = "1.4.1"
val LogbackVersion         = "1.2.10"
val LogstashVersion        = "6.6"

lazy val dependencies         = Seq(
  "com.beachape"                  %% "enumeratum"               % EnumeratumVersion,
  "com.softwaremill.sttp.client3" %% "circe"                    % SttpClientVersion,
  "com.softwaremill.sttp.client3" %% "core"                     % SttpClientVersion,
  "ch.qos.logback"                 % "logback-classic"          % LogbackVersion   ,
  "com.typesafe"                   % "config"                   % ConfigVersion    ,
  "net.logstash.logback"           % "logstash-logback-encoder" % LogstashVersion,
  "io.circe"                      %% "circe-generic"            % CirceVersion     ,
  "io.circe"                      %% "circe-generic-extras"     % CirceVersion     ,
  "io.circe"                      %% "circe-literal"            % CirceVersion     ,
  "io.circe"                      %% "circe-parser"             % CirceVersion     ,
  "io.chrisdavenport"             %% "log4cats-slf4j"           % Log4CatsVersion  ,
  "org.scalacheck"                %% "scalacheck"               % ScalaCheckVersion,

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
    libraryDependencies ++= {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, v)) if v <= 12 =>
          Seq(compilerPlugin(("org.scalamacros" % "paradise" % "2.1.1").cross(CrossVersion.full)))
        case _                       =>
          Nil // if scala 2.13.0-M4 or later, macro annotations merged into scala-reflect https://github.com/scala/scala/pull/6606
      }
    },
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
    scalacOptions ++= PartialFunction
      .condOpt(CrossVersion.partialVersion(scalaVersion.value)) {
        case Some((2, n)) if n >= 13 => Seq("-Ymacro-annotations")
        case Some((2, v)) if v <= 12 =>
          Seq("-Ypartial-unification") // 2.13 is partial-unification by default
      }
      .toList
      .flatten,
    Test / javaOptions += "-Duser.timezone=UTC"
  )
