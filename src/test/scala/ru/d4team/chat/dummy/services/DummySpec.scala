package ru.d4team.chat.dummy.services

import ru.d4team.chat.dummy.Generators.genDummy
import ru.d4team.chat.dummy.models.{Dummy, DummyResult}
import zio._
import zio.mock.Expectation.value
import zio.mock.mockable
import zio.test.Assertion.{anything, equalTo}
import zio.test._

object DummySpec extends ZIOSpecDefault {

  @mockable[ChildDummyServiceA]
  object MockDummyA

  @mockable[ChildDummyServiceB]
  object MockDummyB

  override def spec: Spec[TestEnvironment with Scope, Any] = suite("DummySpec")(
    test("should show how tests work") {
      check(genDummy) { dummy =>
        // Given
        val changeResult = Dummy("whoKnows?")
        val expected     = DummyResult(42)

        // When
        val dependencies = MockDummyA.Change(anything, value(changeResult)) ++
          MockDummyB.Add(equalTo(changeResult), value(expected))

        // Then
        val resultEffect = for {
          service <- ZIO.service[MainDummyService]
          result  <- service.add(dummy)
        } yield assertTrue(result == expected)

        resultEffect.provide(MainDummyService.live, dependencies)
      }
    }
  )
}
