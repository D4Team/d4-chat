package ru.d4team.chat

import ru.d4team.chat.config.AppConfig
import ru.d4team.chat.db.{PgDBMigrator, PgDBTransactor}
import zio._
import zio.logging.backend.SLF4J

object Main extends ZIOAppDefault {

  private val loggerLayer = Runtime.removeDefaultLoggers >>> SLF4J.slf4j

  private val httpApp = ZIO.never.exitCode

  private val mainApp = AppConfig.logConfig *> PgDBMigrator.migrate *> httpApp

  override def run = mainApp.unit.provide(
    // Config
    AppConfig.allConfigs,
    AppConfig.live,
    // DB
    PgDBTransactor.live,
    // Logger
    loggerLayer
  )
}
