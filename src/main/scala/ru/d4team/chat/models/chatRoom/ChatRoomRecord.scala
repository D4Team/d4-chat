package ru.d4team.chat.models.chatRoom

import io.scalaland.chimney.dsl.TransformerOps
import ru.d4team.chat.models.person.PersonRecord

import java.util.UUID

case class ChatRoomRecord(
    chatId: UUID,
    chatName: String,
    info: String,
    members: List[PersonRecord]
)

object ChatRoomRecord {
  def fromChatRoom(r: ChatRoom): ChatRoomRecord =
    r.into[ChatRoomRecord]
      .withFieldComputed(_.members, _.members.map(_.transformInto[PersonRecord]))
      .transform

}
