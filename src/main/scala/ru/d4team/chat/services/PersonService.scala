package ru.d4team.chat.services

import ru.d4team.chat.dao.PersonDAO
import ru.d4team.chat.models.person.Person
import zio._

import java.util.UUID

trait PersonService {
  def addPerson(person: Person): Task[Person]
  def findPerson(id: UUID): Task[Option[Person]]
  def getAll: Task[List[Person]]
}

object PersonService {
  val live: URLayer[PersonDAO, PersonService] = ZLayer.fromFunction(PersonServiceImpl.apply _)
}

final case class PersonServiceImpl(dao: PersonDAO) extends PersonService {
  override def addPerson(person: Person): Task[Person] = dao.addPerson(person)

  override def findPerson(id: UUID): Task[Option[Person]] =
    dao.findPerson(id)

  override def getAll: Task[List[Person]] = dao.getPersons
}
