import Dependencies.Versions._
import sbt._

object Dependencies {

  object Versions {
    lazy val zioVersion           = "2.0.9"
    lazy val zioSqlVersion        = "0.1.1"
    lazy val zioLoggingVersion    = "2.1.7"
    lazy val zioConfigVersion     = "3.0.7"
    lazy val zioMockVersion       = "1.0.0-RC9"
    lazy val monocleVersion       = "3.2.0"
    lazy val logbackVersion       = "1.4.5"
    lazy val kindProjectorVersion = "0.13.2"
    lazy val catsVersion          = "2.9.0"
    lazy val cats3InteropVersion  = "23.0.0.0"
    lazy val liquibaseVersion     = "4.18.0"
  }

  lazy val zio: ModuleID = "dev.zio" %% "zio" % zioVersion

  lazy val zioSql: Seq[ModuleID] = Seq(
    "dev.zio" %% "zio-sql-driver",
    "dev.zio" %% "zio-sql-postgres",
    "dev.zio" %% "zio-sql-jdbc-hickaricp"
  ).map(_ % zioSqlVersion)

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

  lazy val liquibase: ModuleID = "org.liquibase" % "liquibase-core" % liquibaseVersion

  lazy val zioTest: Seq[ModuleID] = Seq(
    "dev.zio" %% "zio-test",
    "dev.zio" %% "zio-test-sbt",
    "dev.zio" %% "zio-test-magnolia"
  ).map(_ % zioVersion % Test)

}
