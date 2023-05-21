package ru.d4team.chat.dummy.models

import zio.json._

case class Dummy(name: String)

object Dummy {
  implicit val decoder: JsonDecoder[Dummy] = DeriveJsonDecoder.gen[Dummy]
  implicit val encoder: JsonEncoder[Dummy] = DeriveJsonEncoder.gen[Dummy]
}
