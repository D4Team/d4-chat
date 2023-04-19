package ru.d4team.chat.utils

import sttp.apispec.openapi.OpenAPI
import sttp.apispec.openapi.circe.yaml._
import zio.test.{TestResult, ZIOSpecDefault, assertTrue}

import scala.io.Source

trait SnapshotSuite { self: ZIOSpecDefault =>

  implicit class OpenAPIOps(openApi: OpenAPI) {
    def matchSnapshot: TestResult = matchSnapshot(self.getClass.getSimpleName.split('$').head)

    def matchSnapshot(fileName: String): TestResult = {
      val snapshotYaml = Source.fromResource(s"snapshots/$fileName.yaml").mkString
      assertTrue(snapshotYaml == openApi.toYaml)
    }
  }
}
