package ru.d4team.chat.db

import org.flywaydb.core.api.MigrationState
import org.flywaydb.core.api.configuration.FluentConfiguration
import ru.d4team.chat.config.PostgresConfig
import zio._


object DBMigrator {

  def migrate: ZIO[PostgresConfig, Throwable, Unit] =
    for {
      config <- ZIO.service[PostgresConfig]
      _ <- ZIO.logInfo(s"Starting the migration for host: ${config.url}")
      count <- migrationEffect(config)
      _ <- ZIO.logInfo(s"Successful migrations: $count")
    } yield ()


  private def migrationEffect(config: PostgresConfig): ZIO[Any, Throwable, Int] =
    for {

      flywayConfig <- ZIO.succeed(PostgresConfig.flywayConfig(config))
      _ <- logValidationErrorsIfAny(flywayConfig)
      _ <- ZIO.logInfo("Migrations validation successful")
      count <- ZIO.succeed(flywayConfig.load().migrate().migrationsExecuted)

      // fail for any statuses except success (in case of missing migration files, etc)
      _ <- ZIO.foreach(flywayConfig.load().info().all().toList) { i =>
        i.getState match {
          case MigrationState.SUCCESS => ZIO.unit
          case e => ZIO.fail(new Error(s"Migration ${i.getDescription} status is not \"SUCCESS\": ${e.toString}"))
        }
      }

    } yield count

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
