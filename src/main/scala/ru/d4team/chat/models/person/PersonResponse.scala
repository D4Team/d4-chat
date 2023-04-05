package ru.d4team.chat.models.person

import io.scalaland.chimney.dsl.TransformerOps
import zio.json._

import java.time.Instant
import java.util.UUID

@jsonMemberNames(SnakeCase) case class PersonResponse(
    personId: UUID,
    name: String,
    birthDate: Instant,
    personInfo: String
)

object PersonResponse {
  implicit val encoder: JsonEncoder[PersonResponse] = DeriveJsonEncoder.gen[PersonResponse]

  def fromPerson(p: Person): PersonResponse = p.transformInto[PersonResponse]
}
