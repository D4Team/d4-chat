package ru.d4team.chat.config

import zio._
import zio.sql.HikariConnectionPoolConfig

final case class PostgresConfig(
    host: String,
    port: Int,
    db: String,
    user: String,
    password: String
) {
  val url = s"jdbc:postgresql://$host:$port/$db"
}

object PostgresConfig {

  val hikariConnectionPoolConfig: ZLayer[PostgresConfig, Throwable, HikariConnectionPoolConfig] = ZLayer {
    for {
      pgConfig  <- ZIO.service[PostgresConfig]
      poolConfig = HikariConnectionPoolConfig(
                     url = pgConfig.url,
                     userName = pgConfig.user,
                     password = pgConfig.password
                   )
    } yield poolConfig
  }

}
