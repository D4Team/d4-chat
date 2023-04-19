package ru.d4team.chat.dummy.api

import ru.d4team.chat.api.BaseController
import ru.d4team.chat.dummy.models.{Dummy, DummyResult}
import ru.d4team.chat.dummy.services.MainDummyService
import sttp.tapir.Endpoint
import sttp.tapir.generic.auto.schemaForCaseClass
import sttp.tapir.json.zio._
import sttp.tapir.server.ziohttp.ZioHttpInterpreter
import sttp.tapir.ztapir._
import zio.http.App
import zio._

trait DummyController extends BaseController {
  def dummyEndpoint: Endpoint[Unit, Dummy, Unit, DummyResult, Any]
}

object DummyController {
  val live: URLayer[MainDummyService, DummyController] = ZLayer.fromFunction(DummyControllerImpl.apply _)
}

final case class DummyControllerImpl(dummyService: MainDummyService) extends DummyController {
  override val dummyEndpoint: Endpoint[Unit, Dummy, Unit, DummyResult, Any] = endpoint
    .in("dummy")
    .post
    .in(jsonBody[Dummy])
    .out(jsonBody[DummyResult])

  override val endpoints: List[Endpoint[Unit, Dummy, Unit, DummyResult, Any]] =
    List(dummyEndpoint).map(_.tags(List("Dummy endpoints")))

  override val route: App[Any] =
    ZioHttpInterpreter().toHttp(dummyEndpoint.zServerLogic(dummyService.add)).withDefaultErrorResponse
}
