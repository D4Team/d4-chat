import Dependencies._
import sbt.Keys.version
import java.time.Instant

scalaVersion := "2.13.10"

Global / onChangedBuildSource := ReloadOnSourceChanges
Global / cancelable           := true

semanticdbEnabled := true
semanticdbVersion := scalafixSemanticdb.revision

maintainer := "D4 Team"
name       := "D4 Chat"
version    := "0.1.0-SNAPSHOT"

scalacOptions ++= Seq(
  "-deprecation",                     // Emit warning and location for usages of deprecated APIs.
  "-explaintypes",                    // Explain type errors in more detail.
  "-feature",                         // Emit warning and location for usages of features that should be imported explicitly.
  "-unchecked",                       // Enable additional warnings where generated code depends on assumptions.
  "-Wdead-code",                      // Warn when dead code is identified.
  "-Wextra-implicit",                 // Warn when more than one implicit parameter section is defined.
  "-Wunused",                         // Warn if something from check list is unused.
  "-Wvalue-discard",                  // Warn when non-Unit expression results are unused.
  "-Ywarn-macros:after",              // Needed for correct implicit resolution.
  "-Wconf:cat=unused-nowarn:s",       // Silence nowarn usage warnings.
  "-Xfatal-warnings",                 // Fail the compilation if there are any warnings.
  "-Ymacro-annotations"              // Bring textual abstraction to the level of definitions.
)

lazy val buildInfoSettings = buildInfoKeys ++= Seq[BuildInfoKey](
  name,
  version,
  BuildInfoKey.action("commit")(git.gitHeadCommit.value),
  BuildInfoKey.action("buildTime")(Instant.now().toString)
)

lazy val root = project
  .in(file("."))
  .withId("d4-chat")
  .enablePlugins(BuildInfoPlugin)
  .enablePlugins(GitVersioning)
  .enablePlugins(JavaAppPackaging)
  .settings(buildInfoSettings)
  .settings(
    scalafmtOnCompile      := true,
    scalafmtLogOnEachError := true,
    scalafixOnCompile      := true
  )
  .settings(addCompilerPlugin(kindProjector))
  .settings(
    libraryDependencies += zio,
    libraryDependencies ++= zioSql,
    libraryDependencies ++= zioConfig,
    libraryDependencies += cats,
    libraryDependencies += cats3Interop,
    libraryDependencies ++= logging,
    libraryDependencies ++= monocle,
    libraryDependencies += liquibase
  )
  .settings(
    libraryDependencies += zioMock,
    libraryDependencies ++= zioTest,
    scalacOptions += "-Wconf:src=*<macro>*&cat=unused:s" // Silence warns of unused things for macro in zio mock
  )
