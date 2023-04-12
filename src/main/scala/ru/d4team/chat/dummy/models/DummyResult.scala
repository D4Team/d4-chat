package ru.d4team.chat.dummy.models

import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}

case class DummyResult(length: Int)

object DummyResult {
  implicit val decoder: JsonDecoder[DummyResult] = DeriveJsonDecoder.gen[DummyResult]
  implicit val encoder: JsonEncoder[DummyResult] = DeriveJsonEncoder.gen[DummyResult]
}
