package ru.d4team.chat.api

import ru.d4team.chat.dummy.api.DummyController
import sttp.tapir.AnyEndpoint
import sttp.apispec.openapi.circe.yaml._
import sttp.tapir.docs.openapi.OpenAPIDocsInterpreter
import sttp.tapir.server.ziohttp.ZioHttpInterpreter
import sttp.tapir.swagger.SwaggerUI
import zio._

trait RestController extends BaseController

object RestController {
  type Env = DummyController & PersonController
  val live: URLayer[Env, RestController] = ZLayer.fromFunction(RestControllerImpl.apply _)
}

final case class RestControllerImpl(dummyController: DummyController, personController: PersonController)
    extends RestController {
  override def endpoints: List[AnyEndpoint] = dummyController.endpoints ++ personController.endpoints

  override def route: http.App[Any] = {
    val openApi       = OpenAPIDocsInterpreter().toOpenAPI(endpoints, "D4 Chat API", "0.1")
    val swaggerRoutes = ZioHttpInterpreter().toHttp(SwaggerUI[Task](openApi.toYaml))

    swaggerRoutes.withDefaultErrorResponse ++ dummyController.route ++ personController.route
  }
}
