package ru.d4team.chat.ws

import zio._
import zio.http._
import zio.http.socket._
import zio.http.ChannelEvent._
import zio.http.ChannelEvent.UserEvent._
import zio.http.model.Method

object WSExample extends ZIOAppDefault {

  // Defaults
  private val Host = "localhost"
  private val Port = 8088

  private val ServConfig = ServerConfig.default.binding(Host, Port)

  // Main logic
  private val socket: Http[Any, Throwable, WebSocketChannelEvent, Unit] =
    Http.collectZIO[WebSocketChannelEvent] {
      case ChannelEvent(_, UserEventTriggered(event))                =>
        event match {
          case HandshakeComplete => Console.printLine("New connection")
          case HandshakeTimeout  => Console.printLine("Connection failure")
        }
      case ChannelEvent(ch, ChannelRead(WebSocketFrame.Text("foo"))) =>
        ch.writeAndFlush(WebSocketFrame.text("bar"))
      case ChannelEvent(ch, ChannelRead(WebSocketFrame.Text("bar"))) =>
        ch.writeAndFlush(WebSocketFrame.text("foo"))
      case ChannelEvent(ch, ChannelRead(WebSocketFrame.Text(text)))  =>
        ch.write(WebSocketFrame.text(text.toUpperCase)).repeatN(2) *> ch.flush
      case ChannelEvent(_, ChannelUnregistered)                      =>
        Console.printLine("Connection closed")
    }

  // API
  private val app: Http[Any, Nothing, Request, Response] =
    Http.collectZIO[Request] { case Method.GET -> !! / "ws" / "example" => socket.toSocketApp.toResponse }

  // Entrypoint
  override val run =
    Server.serve(app).provide(Server.live(ServConfig))

}
