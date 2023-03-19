package ru.d4team.chat.models.person

import io.scalaland.chimney.dsl.TransformerOps

import java.time.Instant
import java.util.UUID

final case class Person(
    personId: UUID,
    name: String,
    birthDate: Instant,
    personInfo: String
)

object Person {
  def fromRecord(r: PersonRecord): Person = r.transformInto[Person]
}