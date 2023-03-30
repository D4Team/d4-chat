package ru.d4team.chat.models.message

import io.scalaland.chimney.dsl.TransformerOps

import java.time.Instant
import java.util.UUID

final case class Message(
    messageId: UUID,
    chatId: UUID,
    writerId: UUID,
    message: String,
    createdAt: Instant
)

object Message {
  def fromMessageRecord(r: MessageRecord): Message = r.transformInto[Message]
}
