package ru.d4team.chat.dummy.api

import ru.d4team.chat.api.BaseController
import ru.d4team.chat.dummy.models.{Dummy, DummyResult}
import ru.d4team.chat.dummy.services.MainDummyService
import sttp.apispec.openapi.circe.yaml._
import sttp.tapir.Endpoint
import sttp.tapir.docs.openapi.OpenAPIDocsInterpreter
import sttp.tapir.generic.auto.schemaForCaseClass
import sttp.tapir.json.zio._
import sttp.tapir.server.ziohttp.ZioHttpInterpreter
import sttp.tapir.swagger.SwaggerUI
import sttp.tapir.ztapir._
import zio._
import zio.http.{HttpApp, Response}

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

  private val allRoutes: HttpApp[Any, Throwable] =
    ZioHttpInterpreter().toHttp(dummyEndpoint.zServerLogic(dummyService.add))

  override val route: HttpApp[Any, Response] = {
    val openApi       = OpenAPIDocsInterpreter().toOpenAPI(endpoints, "Dummy Controller", "0.1")
    val swaggerRoutes = ZioHttpInterpreter().toHttp(SwaggerUI[Task](openApi.toYaml))

    allRoutes ++ swaggerRoutes
  }
    .tapErrorCauseZIO(ZIO.logErrorCause(_))
    .withDefaultErrorResponse
}
