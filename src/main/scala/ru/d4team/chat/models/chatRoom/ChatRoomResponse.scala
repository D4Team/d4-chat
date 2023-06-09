package ru.d4team.chat.models.chatRoom

import io.scalaland.chimney.dsl.TransformerOps
import ru.d4team.chat.models.person.PersonResponse

import java.util.UUID

case class ChatRoomResponse(
    chatId: UUID,
    chatName: String,
    info: String,
    members: List[PersonResponse]
)

object ChatRoomResponse {
  def fromChatRoom(r: ChatRoom): ChatRoomResponse =
    r.into[ChatRoomResponse]
      .withFieldComputed(_.members, _.members.map(_.transformInto[PersonResponse]))
      .transform
}
