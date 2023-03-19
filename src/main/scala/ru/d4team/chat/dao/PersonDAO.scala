package ru.d4team.chat.dao

import io.getquill._
import io.getquill.jdbczio.Quill
import ru.d4team.chat.models.person.{Person, PersonRecord, Persons}
import zio.{Task, URLayer, ZLayer}

import java.util.UUID

object PersonDAO {
  type Env = Quill.Postgres[SnakeCase]
  val live: URLayer[Env, PersonDAO] = ZLayer.fromFunction(PersonDAOImpl.apply _)
}

trait PersonDAO {
  def addPerson(person: Person): Task[Person]
  def findPerson(id: UUID): Task[Option[Person]]
  def getPersons: Task[List[Person]]
}

final case class PersonDAOImpl(quill: Quill.Postgres[SnakeCase]) extends PersonDAO {

  import quill._

  private val schema = dynamicQuerySchema[PersonRecord]("messaging.person")

  override def addPerson(person: Person): Task[Person] = run {
    schema.insertValue(PersonRecord.fromPerson(person))
  }.as(person)

  override def findPerson(id: UUID): Task[Option[Person]] = run {
    schema.filter(_.personId == lift(id)).take(1)
  }.map(_.headOption.map(Person.fromRecord))

  override def getPersons: Task[List[Person]] = run {
    schema
  }.map(Persons.fromRecords)
}
