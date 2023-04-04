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

  private def addPerson(person: Person): Task[Response] =
    personService.addPerson(person).map(PersonResponse.fromPerson).map(person => Response.json(person.toJson))

  override def route: App[Any] = Http
    .collectZIO[Request] {
      case Method.GET -> !! / "persons"            => getAll
      case Method.GET -> !! / "persons" / uuid(id) => findPerson(id)
      case req @ Method.POST -> !! / "persons"     =>
        req.body.asString
          .map(_.fromJson[Person].left.map(fa => new Throwable(fa)))
          .flatMap(ZIO.fromEither(_))
          .flatMap(addPerson)
    }
    .tapErrorCauseZIO(ZIO.logCause(_))
    .mapError(err => Response.json(err.getMessage))
}
