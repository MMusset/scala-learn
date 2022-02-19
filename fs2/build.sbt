import sbt.Keys.scalacOptions

val CatsVersion                            = "2.7.0"
val CatsEffectVersion                      = "3.3.5"
val ScalacheckEffectVersion                = "1.0.3"
val CirceVersion                           = "0.14.1"
val EnumeratumVersion                      = "1.7.0"
val Fs2Version                             = "3.2.4"
val Log4CatsVersion                        = "2.2.0"
val LogbackVersion                         = "1.2.10"
val LogstashVersion                        = "7.0.1"
val MunitVersion                           = "0.7.29"
val MunitCatsVersion                       = "1.0.7"

lazy val testDependencies = Seq(
  "org.typelevel" %% "log4cats-testing"        % Log4CatsVersion         % Test,
  "org.scalameta" %% "munit"                   % MunitVersion            % Test,
  "org.scalameta" %% "munit-scalacheck"        % MunitVersion            % Test,
  "org.typelevel" %% "munit-cats-effect-3"     % MunitCatsVersion        % Test,
  "org.typelevel" %% "scalacheck-effect-munit" % ScalacheckEffectVersion % Test
)

lazy val dependencies = Seq(
  "ch.qos.logback"                 % "logback-classic"          % LogbackVersion,
  "co.fs2"                        %% "fs2-core"                 % Fs2Version,
  "com.beachape"                  %% "enumeratum"               % EnumeratumVersion,
  "com.beachape"                  %% "enumeratum-circe"         % EnumeratumVersion,
  "io.circe"                      %% "circe-generic"            % CirceVersion,
  "io.circe"                      %% "circe-generic-extras"     % CirceVersion,
  "io.circe"                      %% "circe-literal"            % CirceVersion,
  "io.circe"                      %% "circe-parser"             % CirceVersion,
  "net.logstash.logback"           % "logstash-logback-encoder" % LogstashVersion,
  "org.typelevel"                 %% "cats-core"                % CatsVersion,
  "org.typelevel"                 %% "cats-effect"              % CatsEffectVersion
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
