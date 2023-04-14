package ru.d4team.chat.api

import ru.d4team.chat.services.ChatRoomsService
import ru.d4team.chat.services.ChatRoomsService.ChatRoomsMap
import zio._
import zio.http._
import zio.http.ChannelEvent.UserEvent.{HandshakeComplete, HandshakeTimeout}
import zio.http.ChannelEvent.{ChannelRead, ChannelUnregistered, UserEventTriggered}
import zio.http.model.Method
import zio.http.socket.{WebSocketChannelEvent, WebSocketFrame}

trait ChatRoomsController {
  def route: UHttpApp
}

object ChatRoomsController {
  val live: URLayer[ChatRoomsService[ChatRoomsMap], ChatRoomsController] =
    ZLayer.fromFunction(ChatRoomsControllerImpl.apply _)
}

final case class ChatRoomsControllerImpl(chatRooms: ChatRoomsService[ChatRoomsMap]) extends ChatRoomsController {
  override def route: UHttpApp =
    Http.collectZIO[Request] { case Method.GET -> !! / "ws" / "chat" / room / user =>
      chat(room, user).toSocketApp.toResponse
    }

  private def chat(room: String, user: String): Http[Any, Throwable, WebSocketChannelEvent, Unit] =
    Http.collectZIO[WebSocketChannelEvent] {
      case ChannelEvent(ch, UserEventTriggered(HandshakeComplete))     =>
        chatRooms.join(ch, room) *> chatRooms.broadcast(ch, room, s"$user joined the $room chat")
      case ChannelEvent(_, UserEventTriggered(HandshakeTimeout))       =>
        Console.printLine("Connection failure")
      case ChannelEvent(ch, ChannelRead(WebSocketFrame.Text(message))) =>
        chatRooms.broadcast(ch, room, s"$user: $message")
      case ChannelEvent(ch, ChannelUnregistered)                       =>
        chatRooms.leave(ch, room) *> chatRooms.broadcast(ch, room, s"$user left the $room chat")
    }
}
