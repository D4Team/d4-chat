package ru.d4team.chat.api

import zio.json._

@jsonMemberNames(SnakeCase) sealed trait ControllerError {
  def errorMessage: String
}

object ControllerError {
  @jsonMemberNames(SnakeCase) final case class NotFound(errorMessage: String) extends ControllerError
  object NotFound {
    implicit val codec: JsonCodec[NotFound] = DeriveJsonCodec.gen
  }

  @jsonMemberNames(SnakeCase) final case class InternalServerError(errorMessage: String) extends ControllerError
  object InternalServerError {
    implicit val codec: JsonCodec[InternalServerError] = DeriveJsonCodec.gen
  }
}
