package ru.d4team.chat.models.message

import io.scalaland.chimney.dsl.TransformerOps

import java.time.Instant
import java.util.UUID

case class MessageResponse(
    messageId: UUID,
    chatId: UUID,
    writerId: UUID,
    message: String,
    createdAt: Instant
)

object MessageResponse {
  def fromMessage(m: Message): MessageResponse = m.transformInto[MessageResponse]
}
