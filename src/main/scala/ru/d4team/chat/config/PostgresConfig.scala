package ru.d4team.chat.config

import org.flywaydb.core.Flyway
import org.flywaydb.core.api.configuration.FluentConfiguration
import zio.sql.HikariConnectionPoolConfig
import zio.{ RLayer, ZIO, ZLayer }

final case class PostgresConfig(
  host: String,
  port: Int,
  db: String,
  user: String,
  password: String,
  migrationsLocation: String,
  schema: String
) {
  val url = s"jdbc:postgresql://$host:$port/$db"
}

object PostgresConfig {

  val flywayConfig: RLayer[PostgresConfig, FluentConfiguration] = ZLayer.fromFunction(buildFlywayConfig _)
  private def buildFlywayConfig(conf: PostgresConfig): FluentConfiguration =
    Flyway.configure
      .loggers("slf4j")
      .dataSource(
        conf.url,
        conf.user,
        conf.password
      )
      .group(true)
      .outOfOrder(false)
      .locations(conf.migrationsLocation)
      .failOnMissingLocations(true)
      .baselineOnMigrate(true)

  val hikariConnectionPoolConfig: RLayer[PostgresConfig, HikariConnectionPoolConfig] = ZLayer {
    for {
      pgConfig <- ZIO.service[PostgresConfig]
      poolConfig = HikariConnectionPoolConfig(
        url = pgConfig.url,
        userName = pgConfig.user,
        password = pgConfig.password
      )
    } yield poolConfig
  }

}
