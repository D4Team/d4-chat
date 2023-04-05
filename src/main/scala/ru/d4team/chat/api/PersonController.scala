package ru.d4team.chat.api

import ru.d4team.chat.models.person.{Person, PersonResponse}
import ru.d4team.chat.services.PersonService
import zio._
import zio.json.{DecoderOps, EncoderOps}
import zio.http._
import zio.http.model.{Method, Status}

import java.util.UUID

trait PersonController {
  def route: App[Any]
}

object PersonController {
  val live: URLayer[PersonService, PersonController] = ZLayer.fromFunction(PersonControllerImpl.apply _)
}

final case class PersonControllerImpl(personService: PersonService) extends PersonController {
  private def getAll: Task[Response] =
    personService.getAll.map(_.map(PersonResponse.fromPerson)).map(seq => Response.json(seq.toJson))

  private def findPerson(id: UUID): Task[Response] =
    personService.findPerson(id).map(_.map(PersonResponse.fromPerson)).map {
      case Some(person) => Response.json(person.toJson)
      case None         => Response.status(Status.NotFound)
    }

  private def addPerson(req: Request): Task[Response] = for {
    rawBody       <- req.body.asString
    // TODO better to implement PersonRequest which will not include `id` field
    person        <- ZIO.fromEither(rawBody.fromJson[Person].left.map(err => new Throwable(err)))
    addedPerson   <- personService.addPerson(person)
    personResponse = PersonResponse.fromPerson(addedPerson)
  } yield Response.json(personResponse.toJson)

  override def route: App[Any] = Http
    .collectZIO[Request] {
      case Method.GET -> !! / "persons"            => getAll
      case Method.GET -> !! / "persons" / uuid(id) => findPerson(id)
      case req @ Method.POST -> !! / "persons"     => addPerson(req)
    }
    .tapErrorCauseZIO(ZIO.logCause(_))
    .mapError(err => Response.json(err.getMessage)) // TODO handle errors properly
}
