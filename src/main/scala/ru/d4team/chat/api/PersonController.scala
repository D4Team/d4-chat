package ru.d4team.chat.api

import ru.d4team.chat.models.person.{Person, PersonResponse}
import ru.d4team.chat.services.PersonService
import sttp.apispec.openapi.circe.yaml._
import sttp.model.StatusCode
import sttp.tapir.docs.openapi.OpenAPIDocsInterpreter
import sttp.tapir.generic.auto.schemaForCaseClass
import sttp.tapir.json.zio._
import sttp.tapir.server.ziohttp.ZioHttpInterpreter
import sttp.tapir.swagger.SwaggerUI
import sttp.tapir.ztapir._
import sttp.tapir.{AnyEndpoint, Endpoint, EndpointOutput}
import zio._
import zio.http.{App, HttpApp}

import java.time.Instant
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

  private val internalError: EndpointOutput[ControllerError.InternalServerError] =
    statusCode(StatusCode.InternalServerError)
      .and(
        jsonBody[ControllerError.InternalServerError]
          .description("Internal server error")
          .example(ControllerError.InternalServerError("Error on server side"))
      )

  private val personResponseExample1 =
    PersonResponse(
      UUID.fromString("38e80369-9310-4d5a-9bd2-fead18203724"),
      "Example name",
      Instant.parse("2023-04-19T14:30:00Z"),
      "Additional info"
    )
  private val personResponseExample2 =
    PersonResponse(
      UUID.fromString("11dfd0a9-852b-4972-883a-0d227f55c60d"),
      "Name and surname",
      Instant.parse("2002-02-21T14:30:00Z"),
      "Living address"
    )
  private val personRequestExample   = Person(
    UUID.fromString("6fa94734-96af-4318-8276-18f89336e127"),
    "Name to add",
    Instant.parse("1998-06-11T12:30:00Z"),
    "Favourite color"
  )

  override val getAllEndpoint: Endpoint[Unit, Unit, ControllerError.InternalServerError, List[PersonResponse], Any] =
    baseEndpoint.get
      .out(
        jsonBody[List[PersonResponse]]
          .description("All active persons")
          .example(List(personResponseExample1, personResponseExample2))
      )
      .errorOut(internalError)

  private val getAllServerEndpoint: IO[ControllerError.InternalServerError, List[PersonResponse]] =
    personService.getAll.mapBoth(
      err =>
        ControllerError.InternalServerError(
          err.getMessage
        ), // TODO grigorii_berezin: get error message on 5xx only for debug purposes
      _.map(PersonResponse.fromPerson)
    )

  override val findPersonEndpoint: Endpoint[Unit, UUID, ControllerError, PersonResponse, Any] =
    baseEndpoint.get
      .in(path[UUID]("person-id"))
      .out(jsonBody[PersonResponse].description("Founded person").example(personResponseExample1))
      .errorOut(
        oneOf[ControllerError](
          oneOfVariant(internalError),
          oneOfVariant(
            statusCode(StatusCode.NotFound).and(
              jsonBody[ControllerError.NotFound]
                .description("Not found")
                .example(ControllerError.NotFound(s"Person with id: `person-id` does not exist"))
            )
          )
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
      .in(jsonBody[Person].description("Person body to add").example(personRequestExample))
      .out(
        jsonBody[PersonResponse].description("Added person").example(PersonResponse.fromPerson(personRequestExample))
      )
      .errorOut(internalError)

  private def addPersonServiceEndpoint(person: Person): IO[ControllerError.InternalServerError, PersonResponse] =
    personService
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

  override def endpoints: List[AnyEndpoint] =
    List(getAllEndpoint, findPersonEndpoint, addPersonEndpoint).map(_.tag("Person endpoints"))
}
