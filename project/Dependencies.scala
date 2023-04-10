import Dependencies.Versions._
import sbt._

object Dependencies {

  object Versions {
    lazy val zioVersion           = "2.0.10"
    lazy val zioHttpVersion       = "0.0.5"
    lazy val zioJsonVersion       = "0.5.0"
    lazy val zioLoggingVersion    = "2.1.7"
    lazy val zioConfigVersion     = "3.0.7"
    lazy val zioMockVersion       = "1.0.0-RC9"
    lazy val monocleVersion       = "3.2.0"
    lazy val logbackVersion       = "1.4.5"
    lazy val kindProjectorVersion = "0.13.2"
    lazy val catsVersion          = "2.9.0"
    lazy val cats3InteropVersion  = "23.0.0.0"
    lazy val liquibaseVersion     = "4.18.0"
    lazy val flywayVersion        = "9.4.0"
    lazy val chimneyVersion       = "0.7.1"
    lazy val quillVersion         = "4.6.0"
    lazy val postgresqlVersion    = "42.6.0"
  }

  lazy val zio: ModuleID = "dev.zio" %% "zio" % zioVersion

  lazy val zioHttp = "dev.zio" %% "zio-http" % zioHttpVersion

  lazy val zioJson = "dev.zio" %% "zio-json" % zioJsonVersion

  lazy val logging: Seq[ModuleID] = Seq(
    "dev.zio"       %% "zio-logging-slf4j" % zioLoggingVersion,
    "ch.qos.logback" % "logback-classic"   % logbackVersion
  )

  lazy val zioMock = "dev.zio" %% "zio-mock" % zioMockVersion

  lazy val zioConfig: Seq[ModuleID] = Seq(
    "dev.zio" %% "zio-config",
    "dev.zio" %% "zio-config-magnolia",
    "dev.zio" %% "zio-config-typesafe"
  ).map(_ % zioConfigVersion)

  lazy val monocle: Seq[ModuleID] = Seq(
    "dev.optics" %% "monocle-core",
    "dev.optics" %% "monocle-macro"
  ).map(_ % monocleVersion)

  lazy val cats3Interop: ModuleID = "dev.zio" %% "zio-interop-cats" % cats3InteropVersion

  lazy val kindProjector = ("org.typelevel" % "kind-projector" % kindProjectorVersion).cross(CrossVersion.full)

  lazy val cats = "org.typelevel" %% "cats-core" % catsVersion

  lazy val flyway = Seq(
    "org.flywaydb" % "flyway-core",
    "org.flywaydb" % "flyway-maven-plugin"
  ).map(_ % flywayVersion)

  lazy val zioTest: Seq[ModuleID] = Seq(
    "dev.zio" %% "zio-test",
    "dev.zio" %% "zio-test-sbt",
    "dev.zio" %% "zio-test-magnolia"
  ).map(_ % zioVersion % Test)

  lazy val chimney: ModuleID = "io.scalaland" %% "chimney" % chimneyVersion

  lazy val zioQuill: Seq[ModuleID] = Seq(
    "io.getquill"   %% "quill-jdbc-zio" % quillVersion,
    "org.postgresql" % "postgresql"     % postgresqlVersion
  )

}
