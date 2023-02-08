import Dependencies.Versions._
import sbt._

object Dependencies {

  object Versions {
    lazy val zioVersion           = "2.0.5"
    lazy val zioLoggingVersion    = "2.1.7"
    lazy val zioConfigVersion     = "3.0.7"
    lazy val doobieVersion        = "1.0.0-RC2"
    lazy val monocleVersion       = "3.2.0"
    lazy val logbackVersion       = "1.4.5"
    lazy val cats3InteropVersion  = "23.0.0.0"
    lazy val kindProjectorVersion = "0.13.2"
    lazy val liquibaseVersion     = "4.18.0"
    lazy val pgDriverVersion      = "42.5.1"
  }

  lazy val zio: ModuleID = "dev.zio" %% "zio" % zioVersion

  lazy val logging: Seq[ModuleID] = Seq(
    "dev.zio"       %% "zio-logging-slf4j" % zioLoggingVersion,
    "ch.qos.logback" % "logback-classic"   % logbackVersion
  )

  lazy val zioConfig: Seq[ModuleID] = Seq(
    "dev.zio" %% "zio-config",
    "dev.zio" %% "zio-config-magnolia",
    "dev.zio" %% "zio-config-typesafe"
  ).map(_ % zioConfigVersion)

  lazy val doobie: Seq[ModuleID] = Seq(
    "org.tpolecat" %% "doobie-core",
    "org.tpolecat" %% "doobie-postgres",
    "org.tpolecat" %% "doobie-hikari"
  ).map(_ % doobieVersion)

  lazy val monocle: Seq[ModuleID] = Seq(
    "dev.optics" %% "monocle-core",
    "dev.optics" %% "monocle-macro"
  ).map(_ % monocleVersion)

  lazy val cats3Interop: ModuleID = "dev.zio" %% "zio-interop-cats" % cats3InteropVersion

  lazy val kindProjector = ("org.typelevel" % "kind-projector" % kindProjectorVersion).cross(CrossVersion.full)

  lazy val liquibase: ModuleID = "org.liquibase"  % "liquibase-core" % liquibaseVersion
  lazy val pgDriver: ModuleID  = "org.postgresql" % "postgresql"     % pgDriverVersion

}
