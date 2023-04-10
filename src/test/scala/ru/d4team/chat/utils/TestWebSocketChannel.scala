package ru.d4team.chat.utils

import zio._
import zio.http._
import zio.http.socket._
import zio.http.ChannelEvent._

import java.util.UUID

object TestWebSocketChannel {
  def make[T >: TestWebSocketChannel]: UIO[T] =
    for {
      queue <- Queue.unbounded[ChannelEvent.Event[WebSocketFrame]]
      _     <- queue.offer(UserEventTriggered(UserEvent.HandshakeComplete))
    } yield TestWebSocketChannel(queue)
}

final case class TestWebSocketChannel(counterpartEvents: Queue[ChannelEvent.Event[WebSocketFrame]])
    extends WebSocketChannel {

  private val baseId = UUID.randomUUID.toString

  override def id(implicit trace: Trace): String = baseId

  override def awaitClose(implicit trace: Trace): UIO[Unit] = close(await = true).orDie

  override def close(await: Boolean)(implicit trace: Trace): Task[Unit] =
    counterpartEvents.offer(ChannelEvent.ChannelUnregistered).unit

  override def flush(implicit trace: Trace): Task[Unit] = ZIO.unit

  override def write(msg: WebSocketFrame, await: Boolean)(implicit trace: Trace): Task[Unit] =
    counterpartEvents.offer(ChannelEvent.ChannelRead(msg)).unit

  override def writeAndFlush(msg: WebSocketFrame, await: Boolean)(implicit trace: Trace): Task[Unit] =
    counterpartEvents.offer(ChannelEvent.ChannelRead(msg)).unit

  override def autoRead(flag: Boolean)(implicit trace: Trace): UIO[Unit] = ???

  override def contramap[A1](f: A1 => WebSocketFrame): Channel[A1] = ???

  override def isAutoRead(implicit trace: Trace): UIO[Boolean] = ???

  override def read(implicit trace: Trace): UIO[Unit] = ???
}
