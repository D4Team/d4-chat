package ru.d4team.chat.services

import cats.implicits._
import zio._
import zio.http.Channel
import zio.http.socket.WebSocketFrame
import ru.d4team.chat.services.ChatRoomsService._

import scala.collection.concurrent.TrieMap

trait ChatRoomsService[Storage] {
  private[services] def rooms: Storage

  def join(channel: Channel[WebSocketFrame], room: String): UIO[Unit]
  def leave(channel: Channel[WebSocketFrame], room: String): UIO[Unit]
}

object ChatRoomsService {
  type ChatRoomMap  = TrieMap[String, Channel[WebSocketFrame]]
  type ChatRoomsMap = TrieMap[String, ChatRoomMap]

  val mapStorageLive: ULayer[ChatRoomsService[ChatRoomsMap]] = ZLayer.fromZIO {
    ZIO.succeed(TrieMap.empty[String, ChatRoomMap]).map(ChatRoomsServiceMapImpl)
  }
}

final case class ChatRoomsServiceMapImpl(private[services] val rooms: ChatRoomsMap)
    extends ChatRoomsService[ChatRoomsMap] {

  def join(channel: Channel[WebSocketFrame], room: String): UIO[Unit] =
    ZIO.succeed {
      rooms.updateWith(room) {
        case Some(room) => room.addOne(channel.id, channel).some
        case None       => TrieMap(channel.id -> channel).some
      }
    }.unit

  def leave(channel: Channel[WebSocketFrame], room: String): UIO[Unit] =
    ZIO.succeed {
      rooms.updateWith(room) {
        case Some(room) => room.subtractOne(channel.id).some.filter(_.nonEmpty)
        case None       => None // TODO (Aleksei Litkovetc) - Log a leaving from non-existent room
      }
    }.unit
}
