package ru.d4team.chat.models.message

import io.scalaland.chimney.dsl.TransformerOps

import java.time.Instant
import java.util.UUID

final case class Message(
    id: UUID,
    chatRoomId: UUID,
    writerId: UUID,
    createdAt: Instant,
    content: String
)

object Message {
  def fromMessageRecord(r: MessageRecord): Message = r.transformInto[Message]
}
