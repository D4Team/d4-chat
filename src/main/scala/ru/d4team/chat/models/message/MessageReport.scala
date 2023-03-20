package ru.d4team.chat.models.message

import io.scalaland.chimney.dsl.TransformerOps

import java.time.Instant
import java.util.UUID

case class MessageReport(
    id: UUID,
    chatRoomId: UUID,
    writerId: UUID,
    createdAt: Instant,
    content: String
)

object MessageReport {
  def fromMessage(m: Message): MessageReport = m.transformInto[MessageReport]
}
