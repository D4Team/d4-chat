package ru.d4team.chat.models.message

import io.scalaland.chimney.dsl.TransformerOps

import java.time.Instant
import java.util.UUID

final case class MessageRecord(
    messageId: UUID,
    chatId: UUID,
    writerId: UUID,
    message: String,
    createdAt: Instant
)

object MessageRecord {
  def fromMessage(m: Message): MessageRecord = m.transformInto[MessageRecord]
}
