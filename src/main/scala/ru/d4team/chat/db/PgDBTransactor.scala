package ru.d4team.chat.db

import doobie.Transactor
import doobie.hikari.HikariTransactor
import ru.d4team.chat.config.PostgresConfig
import zio._
import zio.interop.catz._

trait PgDBTransactor {
  def xa: Transactor[Task]
}

object PgDBTransactor {
  val live: URLayer[PostgresConfig, PgDBTransactor] = ZLayer.scoped {
    for {
      config     <- ZIO.service[PostgresConfig]
      be         <- ZIO.blockingExecutor
      transactor <- HikariTransactor
                      .newHikariTransactor[Task](
                        driverClassName = "org.postgresql.Driver",
                        url = s"jdbc:postgresql://${config.host}:${config.port}/${config.db}",
                        user = config.user,
                        pass = config.password,
                        connectEC = be.asExecutionContext
                      )
                      .toScopedZIO
                      .orDie
    } yield new PgDBTransactor {
      override def xa: Transactor[Task] = transactor
    }
  }
}
