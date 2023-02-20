package ru.d4team.chat.db

import liquibase.Liquibase
import liquibase.database.DatabaseFactory
import liquibase.database.jvm.JdbcConnection
import liquibase.resource.ClassLoaderResourceAccessor
import zio._
import zio.sql.HikariConnectionPool

object PgDBMigrator {

  private val masterChangeLog = "migration/changeLog.xml"

  val migrate: URIO[HikariConnectionPool, Unit] =
    ZIO.scoped {
      for {
        connectionPool  <- ZIO.service[HikariConnectionPool]
        connection      <- connectionPool.connection
        resourceAccessor = new ClassLoaderResourceAccessor
        jdbcConnection   = new JdbcConnection(connection)
        database         = DatabaseFactory.getInstance.findCorrectDatabaseImplementation(jdbcConnection)
        liquibase        = new Liquibase(masterChangeLog, resourceAccessor, database)
        _               <- ZIO.attemptBlocking(liquibase.update())
      } yield ()
    }.orDie

}
