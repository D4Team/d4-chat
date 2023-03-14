package ru.d4team.chat

import ru.d4team.chat.config.{AppConfig, PostgresConfig}
import ru.d4team.chat.db.DBMigrator
import zio._
import zio.logging.backend.SLF4J

object Main extends ZIOAppDefault {

  private val loggerLayer = Runtime.removeDefaultLoggers >>> SLF4J.slf4j

  private val httpApp = ZIO.never.exitCode

  private val mainApp = AppConfig.logConfig *> DBMigrator.migrate *> httpApp

  override def run: IO[Any, ExitCode] = mainApp.provide(
    // Config
    AppConfig.allConfigs,
    AppConfig.live,
    // DB
    PostgresConfig.flywayConfig,

//    PostgresConfig.hikariConnectionPoolConfig,
//    HikariConnectionPool.live,
    // Logger
    loggerLayer
  )
}
