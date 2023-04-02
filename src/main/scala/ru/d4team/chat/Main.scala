package ru.d4team.chat

import ru.d4team.chat.api.PersonController
import ru.d4team.chat.config.{AppConfig, PostgresConfig, ServerConfig}
import ru.d4team.chat.db.DBMigrator
import ru.d4team.chat.services.PersonService
import zio._
import zio.http._
import zio.http.{ServerConfig => ZioServerConfig}
import zio.logging.backend.SLF4J

object Main extends ZIOAppDefault {

  private val loggerLayer = Runtime.removeDefaultLoggers >>> SLF4J.slf4j

  private val httpApp = for {
    api    <- ZIO.service[PersonController]
    port   <- Server.install(api.route)
    _      <- ZIO.logInfo(s"Started server on port: $port")
    _      <- Console.readLine("Press any key to exit")
    _      <- ZIO.logInfo("Closing...")
  } yield ExitCode.success

  private val mainApp = AppConfig.logConfig *> DBMigrator.migrate *> httpApp

  private val zioConfig = ZLayer.fromFunction { config: ServerConfig =>
    ZioServerConfig.default.port(config.port)
  }

  override def run: IO[Any, ExitCode] = mainApp.provide(
    // Config
    AppConfig.allConfigs,
    AppConfig.live,
    // DB
    PostgresConfig.flywayConfig,
    // Server
    zioConfig,
    Server.live,
    // Persons
    PersonService.live,
    PersonController.live,

//    PostgresConfig.quillLayer,
//    PostgresConfig.dsLayer,
//    PersonDAO.live,
    // Logger
    loggerLayer
  )
}
