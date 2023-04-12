package ru.d4team.chat.api

import sttp.tapir.Endpoint
import zio.http._

trait BaseController {
  def endpoints: List[Endpoint[_, _, _, _, _]]

  def route: App[Any]
}
