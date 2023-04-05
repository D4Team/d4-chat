package ru.d4team.chat.config

import io.getquill.SnakeCase
import io.getquill.jdbczio.Quill
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.configuration.FluentConfiguration
import zio._

import javax.sql.DataSource

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

  val flywayConfig: RLayer[PostgresConfig, FluentConfiguration] =
    ZLayer.fromFunction { conf: PostgresConfig =>
      Flyway.configure
        .sqlMigrationSeparator("_")
        .loggers("slf4j")
        .dataSource(conf.url, conf.user, conf.password)
        .group(true)
        .outOfOrder(false)
        .locations(conf.migrationsLocation)
        .failOnMissingLocations(true)
        .baselineOnMigrate(true)
    }

  val quillLayer: URLayer[DataSource, Quill.Postgres[SnakeCase]] = Quill.Postgres.fromNamingStrategy(SnakeCase)
  val dsLayer: TaskLayer[DataSource]                             = Quill.DataSource.fromPrefix("data_source")

}
