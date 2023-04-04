package ru.d4team.chat

import ru.d4team.chat.api.PersonController
import ru.d4team.chat.config.{AppConfig, PostgresConfig, ServerConfig}
import ru.d4team.chat.dao.PersonDAO
import ru.d4team.chat.db.DBMigrator
import ru.d4team.chat.services.PersonService
import zio._
import zio.http._
import zio.http.{ServerConfig => ZioServerConfig}
import zio.logging.backend.SLF4J

object Main extends ZIOAppDefault {

  private val loggerLayer = Runtime.removeDefaultLoggers >>> SLF4J.slf4j

  private val httpApp = for {
    config   <- ZIO.service[ServerConfig]
    zioConf   = ZioServerConfig.default.binding(config.host, config.port)
    api      <- ZIO.service[PersonController]
    _        <- ZIO.logInfo(s"Started server on http://${config.host}:${config.port}")
    exitCode <- Server.serve(api.route).provide(ZioServerConfig.live(zioConf), Server.live).exitCode
  } yield exitCode

  private val mainApp = AppConfig.logConfig *> DBMigrator.migrate *> httpApp

  override def run: IO[Any, ExitCode] = mainApp.provide(
    // Config
    AppConfig.allConfigs,
    AppConfig.live,
    // DB
    PostgresConfig.flywayConfig,
    // Persons
    PersonDAO.live,
    PersonService.live,
    PersonController.live,
    PostgresConfig.quillLayer,
    PostgresConfig.dsLayer,
    // Logger
    loggerLayer
  )
}
