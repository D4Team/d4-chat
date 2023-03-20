package ru.d4team.chat.models.message

import io.scalaland.chimney.dsl.TransformerOps

import java.time.Instant
import java.util.UUID

final case class MessageRecord(
    id: UUID,
    chatRoomId: UUID,
    writerId: UUID,
    createdAt: Instant,
    content: String
)

object MessageRecord {
  def fromMessage(m: Message): MessageRecord = m.transformInto[MessageRecord]
}
