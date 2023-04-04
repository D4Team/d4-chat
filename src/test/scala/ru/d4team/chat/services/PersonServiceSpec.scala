package ru.d4team.chat.services

import ru.d4team.chat.dao.PersonDAO
import ru.d4team.chat.Generators.{genPerson, genPersonList}
import zio._
import zio.mock.Expectation._
import zio.mock.mockable
import zio.test.Assertion._
import zio.test.TestAspect.{nondeterministic, parallel}
import zio.test._

import java.util.UUID

object PersonServiceSpec extends ZIOSpecDefault {
  @mockable[PersonDAO]
  object MockPersonDAO

  override def spec: Spec[TestEnvironment with Scope, Any] = suite("PersonServiceSpec")(
    test("should add person and return it") {
      check(genPerson) { person =>
        // When
        val dependencies = MockPersonDAO.AddPerson(equalTo(person), value(person))

        // Then
        val resultEffect = for {
          service <- ZIO.service[PersonService]
          result <- service.addPerson(person)
        } yield assertTrue(result == person)

        resultEffect.provide(PersonService.live, dependencies)
      }
    },
    test("should find one person by id") {
      check(genPerson) { person =>
        // Given
        val id = person.personId
        val expectedResult = Option(person)

        // When
        val dependencies = MockPersonDAO.FindPerson(equalTo(id), value(expectedResult))

        // Then
        val resultEffect = for {
          service <- ZIO.service[PersonService]
          result <- service.findPerson(id)
        } yield assertTrue(result == expectedResult)

        resultEffect.provide(PersonService.live, dependencies)
      }
    },
    test("should get all persons") {
      check(genPersonList) { expectedResult =>
        // When
        val dependencies = MockPersonDAO.GetPersons(value(expectedResult))

        // Then
        val resultEffect = for {
          service <- ZIO.service[PersonService]
          result <- service.getAll
        } yield assertTrue(result == expectedResult)

        resultEffect.provide(PersonService.live, dependencies)
      }
    }
  ) @@ parallel @@ nondeterministic
}
