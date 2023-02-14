package ru.d4team.chat

import ru.d4team.chat.config.{AppConfig, PostgresConfig}
import ru.d4team.chat.db.PgDBMigrator
import zio._
import zio.logging.backend.SLF4J
import zio.sql.HikariConnectionPool

object Main extends ZIOAppDefault {

  private val loggerLayer = Runtime.removeDefaultLoggers >>> SLF4J.slf4j

  private val httpApp = ZIO.never.exitCode

  private val mainApp = AppConfig.logConfig *> PgDBMigrator.migrate *> httpApp

  override def run: IO[Any, Unit] = mainApp.unit.provide(
    // Config
    AppConfig.allConfigs,
    AppConfig.live,
    // DB
    PostgresConfig.hikariConnectionPoolConfig,
    HikariConnectionPool.live,
    // Logger
    loggerLayer
  )
}
