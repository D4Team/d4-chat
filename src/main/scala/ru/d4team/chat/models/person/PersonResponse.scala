package ru.d4team.chat.models.person

import io.scalaland.chimney.dsl.TransformerOps

import java.time.Instant
import java.util.UUID

case class PersonResponse(
    personId: UUID,
    name: String,
    birthDate: Instant,
    personInfo: String
)

object PersonResponse {
  def fromPerson(p: Person): PersonResponse = p.transformInto[PersonResponse]
}
