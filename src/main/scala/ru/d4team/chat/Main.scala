package ru.d4team.chat

import ru.d4team.chat.api.{ChatRoomsController, PersonController}
import ru.d4team.chat.config.{AppConfig, PostgresConfig, ServerConfig}
import ru.d4team.chat.dao.PersonDAO
import ru.d4team.chat.db.DBMigrator
import ru.d4team.chat.services.{ChatRoomsService, PersonService}
import zio._
import zio.http._
import zio.http.{ServerConfig => ZioServerConfig}
import zio.logging.backend.SLF4J

object Main extends ZIOAppDefault {

  private val loggerLayer = Runtime.removeDefaultLoggers >>> SLF4J.slf4j

  private val httpApp = for {
    config       <- ZIO.service[ServerConfig]
    personApi    <- ZIO.service[PersonController]
    chatRoomsApi <- ZIO.service[ChatRoomsController]
    routes        = personApi.route ++ chatRoomsApi.route
    _            <- Server.install(routes)
    _            <- ZIO.logInfo(s"Started server on http://${config.host}:${config.port}")
    exitCode     <- ZIO.never.exitCode
  } yield exitCode

  private val zioConfig = ZLayer.fromFunction { config: ServerConfig =>
    ZioServerConfig.default.binding(config.host, config.port)
  }

  private val mainApp = AppConfig.logConfig *> DBMigrator.migrate *> httpApp

  override def run: IO[Any, ExitCode] = mainApp.provide(
    // Config
    AppConfig.allConfigs,
    AppConfig.live,
    // DB
    PostgresConfig.flywayConfig,
    PostgresConfig.quillLayer,
    PostgresConfig.dsLayer,
    // Server
    zioConfig,
    Server.live,
    // Persons
    PersonDAO.live,
    PersonService.live,
    PersonController.live,
    // Chat rooms
    ChatRoomsService.mapStorageLive,
    ChatRoomsController.live,
    // Logger
    loggerLayer
  )
}
