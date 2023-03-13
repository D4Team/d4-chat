package ru.d4team.chat.db

import org.flywaydb.core.Flyway
import org.flywaydb.core.api.MigrationState
import org.flywaydb.core.api.configuration.FluentConfiguration
import org.flywaydb.core.api.output.ValidateResult
import zio._

object DBMigrator {

  def migrate: RIO[FluentConfiguration, Unit] =
    for {
      flywayConfig <- ZIO.service[FluentConfiguration]
      flyway       <- ZIO.attempt(flywayConfig.load)
      _            <- ZIO.logInfo(s"Starting the migration for host: ${flywayConfig.getUrl}")
      _            <- logValidationErrorsIfAny(flywayConfig)
      _            <- ZIO.logInfo("Migrations validation successful")
      count        <- ZIO.attemptBlocking(flyway.migrate.migrationsExecuted)
      _            <- checkStatesForSuccess(flyway)
      _            <- ZIO.logInfo(s"Successful migrations: $count")
    } yield ()

  private def checkStatesForSuccess(flyway: Flyway): Task[Unit] =
    ZIO.attemptBlocking(flyway.info.all).flatMap { infos =>
      ZIO.foreachDiscard(infos) { info =>
        lazy val error = new Error(s"Migration ${info.getDescription} status is not \"SUCCESS\": ${info.getState}")
        ZIO.fail(error).when(info.getState != MigrationState.SUCCESS)
      }
    }

  private def logValidationErrorsIfAny(conf: FluentConfiguration): ZIO[Any, Throwable, ValidateResult] =
    ZIO
      .attemptBlocking(conf.ignoreMigrationPatterns("*:pending").load.validateWithResult)
      .filterOrElseWith(_.validationSuccessful) { validateResult =>
        ZIO.fail(new Error(s"Migrations validation failed:\n${validateResult.getAllErrorMessages}"))
      }

}
