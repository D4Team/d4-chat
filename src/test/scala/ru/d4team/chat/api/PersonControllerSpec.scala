package ru.d4team.chat.api

import ru.d4team.chat.Generators.{genPerson, genPersonList}
import ru.d4team.chat.models.person.PersonResponse
import ru.d4team.chat.services.PersonService
import ru.d4team.chat.utils.{BodyExtractorSyntax, TestUtils}
import zio._
import zio.http._
import zio.http.model.Status
import zio.json._
import zio.mock.Expectation.{failure, value}
import zio.mock.mockable
import zio.test.Assertion.equalTo
import zio.test.TestAspect.{nondeterministic, parallel, silentLogging}
import zio.test._

object PersonControllerSpec extends ZIOSpecDefault with BodyExtractorSyntax with TestUtils {
  @mockable[PersonService]
  object MockPersonService

  override def spec: Spec[TestEnvironment with Scope, Any] = suite("PersonControllerSpec")(
    test("should return all persons") {
      check(genPersonList) { persons =>
        // Given
        val request = Request.get(URL(!! / "persons"))

        // When
        val dependencies = MockPersonService.GetAll(value(persons))

        // Then
        val resultEffect = for {
          app               <- ZIO.service[PersonController]
          response          <- app.route.runZIO(request)
          extractedResponse <- response.body.extractTo[List[PersonResponse]]
        } yield assertTrue(extractedResponse == persons.map(PersonResponse.fromPerson))

        resultEffect.provide(PersonController.live, dependencies)
      }
    },
    test("should return found person by id") {
      check(genPerson) { person =>
        // Given
        val request = Request.get(URL(!! / "persons" / person.personId.toString))

        // When
        val dependencies = MockPersonService.FindPerson(equalTo(person.personId), value(Option(person)))

        // Then
        val resultEffect = for {
          app               <- ZIO.service[PersonController]
          response          <- app.route.runZIO(request)
          extractedResponse <- response.body.extractTo[PersonResponse]
        } yield assertTrue(extractedResponse == PersonResponse.fromPerson(person))

        resultEffect.provide(PersonController.live, dependencies)
      }
    },
    test("should return 404 if nothing is found") {
      check(Gen.uuid) { id =>
        // Given
        val request = Request.get(URL(!! / "persons" / id.toString))

        // When
        val dependencies = MockPersonService.FindPerson(equalTo(id), value(None))

        // Then
        val resultEffect = for {
          app      <- ZIO.service[PersonController]
          response <- app.route.runZIO(request)
        } yield assertTrue(response.status == Status.NotFound)

        resultEffect.provide(PersonController.live, dependencies)
      }
    },
    test("should return created person") {
      check(genPerson) { person =>
        // Given
        val request = Request.post(Body.fromString(person.toJson), URL(!! / "persons"))

        // When
        val dependencies = MockPersonService.AddPerson(equalTo(person), value(person))

        // Then
        val resultEffect = for {
          app               <- ZIO.service[PersonController]
          response          <- app.route.runZIO(request)
          extractedResponse <- response.body.extractTo[PersonResponse]
        } yield assertTrue(extractedResponse == PersonResponse.fromPerson(person))

        resultEffect.provide(PersonController.live, dependencies)
      }
    },
    test("should return error message") {
      check(Gen.alphaNumericString) { errorMessage =>
        // Given
        val request = Request.get(URL(!! / "persons"))

        // When
        val dependencies = MockPersonService.GetAll(failure(new Throwable(errorMessage)))

        // Then
        val resultEffect = for {
          app      <- ZIO.service[PersonController]
          response <- app.route.runZIO(request).expectFailure
          result   <- response.body.asString
        } yield assertTrue(response.status == Status.InternalServerError) && assertTrue(result == errorMessage)

        resultEffect.provide(PersonController.live, dependencies)
      }
    },
    test("should return error on wrong request body") {
      check(Gen.alphaNumericString) { wrongBody =>
        // Given
        val request = Request.post(Body.fromString(wrongBody), URL(!! / "persons"))

        // Then
        val resultEffect = for {
          app      <- ZIO.service[PersonController]
          response <- app.route.runZIO(request).expectFailure
        } yield assertTrue(response.status == Status.InternalServerError)

        resultEffect.provide(PersonController.live, MockPersonService.empty)
      }
    }
  ) @@ parallel @@ nondeterministic @@ silentLogging
}
