package ru.d4team.chat.config

import monocle.macros.GenLens
import zio._
import zio.config.magnolia._
import zio.config.typesafe._
import zio.config._

final case class AppConfig(
    server: ServerConfig,
    postgres: PostgresConfig
)

object AppConfig {

  type AllConfigs = ServerConfig with PostgresConfig

  implicit private val configDescriptor = Descriptor[AppConfig]
    .mapClassName(toSnakeCase)
    .mapFieldName(toSnakeCase)

  val live: ULayer[AppConfig] = TypesafeConfig.fromResourcePath(descriptor[AppConfig]).orDie

  val allConfigs: URLayer[AppConfig, AllConfigs] = subConf(_.server) >+> subConf(_.postgres)

  private def hidePasswords(config: AppConfig): AppConfig = {
    val setters = Seq(GenLens[AppConfig](_.postgres.password))
    setters.foldLeft(config)((conf, setter) => setter.replace("*****")(conf))
  }

  val logConfig: URIO[AppConfig, Unit] =
    for {
      appConfig <- ZIO.service[AppConfig].map(hidePasswords)
      config    <- ZIO
                     .fromEither(write(descriptor[AppConfig], appConfig))
                     .orDieWith(msg => new RuntimeException(s"Can't write config: $msg"))
      _         <- ZIO.logInfo(s"Application config:\n${config.toHoconString}")
    } yield ()

  private def subConf[T: Tag](accessor: AppConfig => T): URLayer[AppConfig, T] =
    ZLayer.fromFunction(accessor)

}
