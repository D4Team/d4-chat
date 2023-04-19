package ru.d4team.chat.api

import sttp.tapir.AnyEndpoint
import zio.http._

trait BaseController {
  def endpoints: List[AnyEndpoint]

  def route: App[Any]
}
