package ru.d4team.chat.api

import ru.d4team.chat.models.person.{Person, PersonResponse}
import ru.d4team.chat.services.PersonService
import sttp.apispec.openapi.circe.yaml._
import sttp.model.StatusCode
import sttp.tapir.Endpoint
import sttp.tapir.docs.openapi.OpenAPIDocsInterpreter
import sttp.tapir.generic.auto.schemaForCaseClass
import sttp.tapir.json.zio._
import sttp.tapir.server.ziohttp.ZioHttpInterpreter
import sttp.tapir.swagger.SwaggerUI
import sttp.tapir.ztapir._
import zio._
import zio.http.{App, HttpApp}

import java.util.UUID

trait PersonController extends BaseController {
  def getAllEndpoint: Endpoint[Unit, Unit, ControllerError.InternalServerError, List[PersonResponse], Any]
  def findPersonEndpoint: Endpoint[Unit, UUID, ControllerError, PersonResponse, Any]
  def addPersonEndpoint(): Endpoint[Unit, Person, ControllerError.InternalServerError, PersonResponse, Any]
}

object PersonController {
  val live: URLayer[PersonService, PersonController] = ZLayer.fromFunction(PersonControllerImpl.apply _)
}

final case class PersonControllerImpl(personService: PersonService) extends PersonController {
  private val baseEndpoint = endpoint.in("persons")

  override val getAllEndpoint: Endpoint[Unit, Unit, ControllerError.InternalServerError, List[PersonResponse], Any] =
    baseEndpoint.get
      .out(jsonBody[List[PersonResponse]])
      .errorOut(
        statusCode(StatusCode.InternalServerError)
          .and(jsonBody[ControllerError.InternalServerError].description("Internal server error"))
      )

  private val getAllServerEndpoint: IO[ControllerError.InternalServerError, List[PersonResponse]] = personService.getAll.mapBoth(
    err => ControllerError.InternalServerError(err.getMessage), // TODO grigorii_berezin: get error message on 5xx only for debug purposes
    _.map(PersonResponse.fromPerson)
  )

  override val findPersonEndpoint: Endpoint[Unit, UUID, ControllerError, PersonResponse, Any] =
    baseEndpoint.get
      .in(path[UUID]("person id"))
      .out(jsonBody[PersonResponse])
      .errorOut(
        oneOf[ControllerError](
          oneOfVariant(
            statusCode(StatusCode.InternalServerError)
              .and(
                jsonBody[ControllerError.InternalServerError].description("Internal server error")
              )
          ),
          oneOfVariant(statusCode(StatusCode.NotFound).and(jsonBody[ControllerError.NotFound].description("Not found")))
        )
      )

  private def findPersonServerEndpoint(id: UUID): IO[ControllerError, PersonResponse] =
    personService
      .findPerson(id)
      .foldZIO(
        err => ZIO.fail(ControllerError.InternalServerError(err.toString)),
        {
          case Some(person) => ZIO.succeed(PersonResponse.fromPerson(person))
          case None         => ZIO.fail(ControllerError.NotFound(s"Person with id: $id does not exist"))
        }
      )

  override val addPersonEndpoint: Endpoint[Unit, Person, ControllerError.InternalServerError, PersonResponse, Any] =
    baseEndpoint.post
      .in(jsonBody[Person])
      .out(jsonBody[PersonResponse])
      .errorOut(
        statusCode(StatusCode.InternalServerError)
          .and(jsonBody[ControllerError.InternalServerError].description("Internal server error"))
      )

  private def addPersonServiceEndpoint(person: Person): IO[ControllerError.InternalServerError, PersonResponse] = personService
    .addPerson(person)
    .mapBoth(err => ControllerError.InternalServerError(err.getMessage), PersonResponse.fromPerson)

  private val allRoutes: HttpApp[Any, Throwable] = ZioHttpInterpreter().toHttp(
    List(
      getAllEndpoint.zServerLogic(_ => getAllServerEndpoint),
      findPersonEndpoint.zServerLogic(id => findPersonServerEndpoint(id)),
      addPersonEndpoint.zServerLogic(p => addPersonServiceEndpoint(p))
    )
  )

  override def route: App[Any] = {
    val openApi       = OpenAPIDocsInterpreter().toOpenAPI(endpoints, "Person Controller", "0.1")
    val swaggerRoutes = ZioHttpInterpreter().toHttp(SwaggerUI[Task](openApi.toYaml))

    allRoutes ++ swaggerRoutes
  }.withDefaultErrorResponse

  override def endpoints: List[Endpoint[_, _, _, _, _]] =
    List(getAllEndpoint, findPersonEndpoint, addPersonEndpoint).map(_.tag("Person endpoints"))
}
