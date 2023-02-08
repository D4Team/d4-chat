package ru.d4team.chat.db

import doobie._
import doobie.implicits._
import liquibase.Liquibase
import liquibase.database.DatabaseFactory
import liquibase.database.jvm.JdbcConnection
import liquibase.resource.ClassLoaderResourceAccessor
import zio._
import zio.interop.catz._

object PgDBMigrator {

  private val masterChangeLog = "migration/changeLog.xml"

  private val migration = FC.raw { connection =>
    val resourceAccessor = new ClassLoaderResourceAccessor
    val jdbcConnection   = new JdbcConnection(connection)
    val database         = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(jdbcConnection)
    val liquibase        = new Liquibase(masterChangeLog, resourceAccessor, database)
    liquibase.update()
  }

  val migrate: URIO[PgDBTransactor, Unit] = ZIO.serviceWith[PgDBTransactor](_.xa).flatMap(migration.transact(_)).orDie

}
