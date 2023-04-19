package ru.d4team.chat.api

import zio.json._

sealed trait ControllerError

object ControllerError {
  final case class NotFound(msg: String) extends ControllerError
  object NotFound {
    implicit val codec: JsonCodec[NotFound] = DeriveJsonCodec.gen
  }

  final case class InternalServerError(msg: String) extends ControllerError
  object InternalServerError {
    implicit val codec: JsonCodec[InternalServerError] = DeriveJsonCodec.gen
  }
}
