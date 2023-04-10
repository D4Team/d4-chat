package ru.d4team.chat.models.person

import io.scalaland.chimney.dsl.TransformerOps
import zio.json._

import java.time.Instant
import java.util.UUID

@jsonMemberNames(SnakeCase) final case class Person(
    personId: UUID,
    name: String,
    birthDate: Instant,
    personInfo: String
)

object Person {
  implicit val decoder: JsonDecoder[Person] = DeriveJsonDecoder.gen[Person]
  implicit val encoder: JsonEncoder[Person] = DeriveJsonEncoder.gen[Person]

  def fromRecord(r: PersonRecord): Person = r.transformInto[Person]
}

object Persons {
  def fromRecords(records: List[PersonRecord]): List[Person] = records.map(Person.fromRecord)
}
