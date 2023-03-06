package ru.d4team.chat.db

import org.flywaydb.core.api.MigrationState
import org.flywaydb.core.api.configuration.FluentConfiguration
import zio._


object DBMigrator {

  def migrate: ZIO[FluentConfiguration, Throwable, Unit] =
    for {
      flywayConfig <- ZIO.service[FluentConfiguration]
      _            <- ZIO.logInfo(s"Starting the migration for host: ${flywayConfig.getUrl}")
      _            <- logValidationErrorsIfAny(flywayConfig)
      _            <- ZIO.logInfo("Migrations validation successful")
      count        <- succeedMigrationCount(flywayConfig)
      _            <- checkStatesForSuccess(flywayConfig)
      _            <- ZIO.logInfo(s"Successful migrations: $count")
    } yield ()

  private def succeedMigrationCount(config: FluentConfiguration): IO[Throwable, Int] = {
    ZIO.succeed(config.load().migrate().migrationsExecuted)
  }

  private def checkStatesForSuccess(config: FluentConfiguration): IO[Error, List[Unit]] = {
    val infoWithStates = config.load().info().all().toList.map(info => (info, info.getState))
    ZIO.foreach(infoWithStates) {
      case (_, MigrationState.SUCCESS) => ZIO.unit
      case (info, e) => ZIO.fail(new Error(s"Migration ${info.getDescription} status is not \"SUCCESS\": ${e.toString}"))
    }
  }
  private def logValidationErrorsIfAny(flywayConfig: FluentConfiguration): ZIO[Any, Throwable, Unit] =
    for {
      validated <- ZIO.succeed(
        flywayConfig
          .ignoreMigrationPatterns("*:pending")
          .load()
          .validateWithResult
      )
      _ <- ZIO.when(!validated.validationSuccessful)(
        ZIO.fail(new Error(validated.getAllErrorMessages + "\n Migrations validation failed"))
      )
    } yield ()

}
