package ru.d4team.chat.services

import ru.d4team.chat.services.ChatRoomsService._
import ru.d4team.chat.utils._
import zio._
import zio.test._

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
    }
  ) @@ nondeterministic @@ parallel
}
