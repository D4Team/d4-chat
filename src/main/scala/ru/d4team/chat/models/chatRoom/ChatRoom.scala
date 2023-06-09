package ru.d4team.chat.models.chatRoom

import io.scalaland.chimney.dsl.TransformerOps
import ru.d4team.chat.models.person.Person

import java.util.UUID

final case class ChatRoom(
    chatId: UUID,
    chatName: String,
    info: String,
    members: List[Person]
)

object ChatRoom {
  def fromRecord(r: ChatRoomRecord): ChatRoom =
    r.into[ChatRoom]
      .withFieldComputed(_.members, _.members.map(_.transformInto[Person]))
      .transform
}
