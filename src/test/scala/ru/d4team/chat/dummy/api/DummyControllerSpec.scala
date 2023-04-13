package ru.d4team.chat.dummy.api

import ru.d4team.chat.dummy.Generators.genDummy
import ru.d4team.chat.dummy.models.DummyResult
import ru.d4team.chat.dummy.services.MainDummyService
import ru.d4team.chat.utils.BodyExtractorSyntax._
import zio.http._
import zio.json._
import zio.mock.Expectation._
import zio.mock.mockable
import zio.test.Assertion.equalTo
import zio.test._
import zio.{Scope, ZIO}

object DummyControllerSpec extends ZIOSpecDefault {
  @mockable[MainDummyService]
  object MainDummyService

  override def spec: Spec[TestEnvironment with Scope, Any] = suite("DummyControllerSpec")(
    test("should return dummy response") {
      check(genDummy) { dummy =>
        // Given
        val request  = Request.post(Body.fromString(dummy.toJson), URL(!! / "dummy"))
        val response = DummyResult(dummy.name.length)

        // When
        val dependencies = MainDummyService.Add(equalTo(dummy), value(response))

        // Then
        val resultEffect = for {
          app               <- ZIO.service[DummyController]
          actualResponse    <- app.route.runZIO(request)
          extractedResponse <- actualResponse.body.extractTo[DummyResult]
        } yield assertTrue(extractedResponse == response)

        resultEffect.provide(DummyController.live, dependencies)
      }
    },
    test("should open swagger") {
      // Given
      val request = Request.get(URL(!! / "docs" / "docs.yaml"))

      // Then
      val resultEffect = for {
        app      <- ZIO.service[DummyController]
        response <- app.route.runZIO(request)
        content  <- response.body.asString
      } yield assertTrue(response.status.isSuccess, content.startsWith("openapi: 3.0.3"))

      resultEffect.provide(DummyController.live, MainDummyService.empty)
    }
  )
}
