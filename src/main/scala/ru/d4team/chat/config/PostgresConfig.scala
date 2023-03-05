package ru.d4team.chat.config

import org.flywaydb.core.Flyway
import org.flywaydb.core.api.configuration.FluentConfiguration

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
  def flywayConfig(conf: PostgresConfig): FluentConfiguration =
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

}
