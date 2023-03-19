package ru.d4team.chat.models.person

import io.scalaland.chimney.dsl.TransformerOps

import java.time.Instant
import java.util.UUID

final case class PersonRecord(
    personId: UUID,
    name: String,
    birthDate: Instant,
    personInfo: String
)

object PersonRecord {
  def fromPerson(p: Person): PersonRecord = p.transformInto[PersonRecord]
}
