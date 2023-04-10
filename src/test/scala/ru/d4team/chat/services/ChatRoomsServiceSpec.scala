package ru.d4team.chat.services

import cats.implicits._
import ru.d4team.chat.services.ChatRoomsService._
import ru.d4team.chat.utils._
import zio._
import zio.http.ChannelEvent.ChannelRead
import zio.http.socket.WebSocketFrame
import zio.test._
import zio.test.Assertion._
import zio.interop.catz.core._

import scala.collection.concurrent.TrieMap
import zio.test.TestAspect._

object ChatRoomsServiceSpec extends ZIOSpecDefault {
  override def spec: Spec[TestEnvironment with Scope, Any] = suite("ChatRoomsServiceSpec")(
    test("'join' should create a new room for a first participant") {
      check(Gen.alphaNumericString) { room =>
        val testF = for {
          // given
          chatRooms    <- ZIO.service[ChatRoomsService[ChatRoomsMap]]
          channel      <- TestWebSocketChannel.make
          expectedRoom  = TrieMap(channel.id -> channel)
          expectedRooms = TrieMap(room -> expectedRoom)

          // then
          initRoomsCnt = chatRooms.rooms.size
          _           <- chatRooms.join(channel, room)
        } yield assertTrue(initRoomsCnt == 0) &&
          assertTrue(chatRooms.rooms == expectedRooms)

        testF.provide(ChatRoomsService.mapStorageLive)
      }
    },
    test("'join' should add a new participant to existing room") {
      check(Gen.alphaNumericString) { room =>
        val testF = for {
          // given
          chatRooms    <- ZIO.service[ChatRoomsService[ChatRoomsMap]]
          channel      <- TestWebSocketChannel.make
          newChannel   <- TestWebSocketChannel.make
          expectedRoom  = TrieMap(channel.id -> channel, newChannel.id -> newChannel)
          expectedRooms = TrieMap(room -> expectedRoom)

          // then
          _ <- chatRooms.join(channel, room)
          _ <- chatRooms.join(newChannel, room)
        } yield assertTrue(chatRooms.rooms == expectedRooms)

        testF.provide(ChatRoomsService.mapStorageLive)
      }
    },
    test("'leave' should remove a participant from a chat room") {
      check(Gen.alphaNumericString) { room =>
        val testF = for {
          // given
          chatRooms    <- ZIO.service[ChatRoomsService[ChatRoomsMap]]
          channel      <- TestWebSocketChannel.make
          newChannel   <- TestWebSocketChannel.make
          _            <- chatRooms.join(channel, room)
          _            <- chatRooms.join(newChannel, room)
          expectedRoom  = TrieMap(channel.id -> channel)
          expectedRooms = TrieMap(room -> expectedRoom)

          // then
          _ <- chatRooms.leave(newChannel, room)
        } yield assertTrue(chatRooms.rooms == expectedRooms)

        testF.provide(ChatRoomsService.mapStorageLive)
      }
    },
    test("'leave' should remove a chat room after a last participant left it") {
      check(Gen.alphaNumericString) { room =>
        val testF = for {
          // given
          chatRooms <- ZIO.service[ChatRoomsService[ChatRoomsMap]]
          channel   <- TestWebSocketChannel.make
          _         <- chatRooms.join(channel, room)

          // then
          _ <- chatRooms.leave(channel, room)
        } yield assertTrue(chatRooms.rooms == TrieMap.empty[String, ChatRoomMap])

        testF.provide(ChatRoomsService.mapStorageLive)
      }
    },
    test("'broadcast' should send a message from one channel to others in the same room") {
      check(Gen.alphaNumericString, Gen.alphaNumericString, Gen.int(1, 10)) { (room, message, size) =>
        val testF =
          for {
            // given
            chatRooms     <- ZIO.service[ChatRoomsService[ChatRoomsMap]]
            mainChannel   <- TestWebSocketChannel.make
            mainChannels  <- List.fill(size)(TestWebSocketChannel.make).sequence
            otherChannels <- List.fill(size)(TestWebSocketChannel.make).sequence
            _             <- chatRooms.join(mainChannel, room)
            _             <- ZIO.foreachParDiscard(mainChannels)(chatRooms.join(_, room))
            _             <- ZIO.foreachParDiscard(otherChannels)(chatRooms.join(_, s"$room-2")) // add participants to another room
            messageEvent   = ChannelRead(WebSocketFrame.text(message))

            // then
            _              <- chatRooms.broadcast(mainChannel, room, message)
            channelEvents  <- mainChannel.asInstanceOf[TestWebSocketChannel].counterpartEvents.takeAll
            channelsEvents <- mainChannels.map(_.asInstanceOf[TestWebSocketChannel].counterpartEvents.takeAll).sequence
            otherEvents    <- otherChannels.map(_.asInstanceOf[TestWebSocketChannel].counterpartEvents.takeAll).sequence
          } yield assert(channelEvents)(not(hasLast(equalTo(messageEvent)))) &&
            assert(channelsEvents)(forall(hasLast(equalTo(messageEvent)))) &&
            assert(otherEvents)(forall(not(hasLast(equalTo(messageEvent)))))

        testF.provide(ChatRoomsService.mapStorageLive)
      }
    }
  ) @@ nondeterministic @@ parallel
}
