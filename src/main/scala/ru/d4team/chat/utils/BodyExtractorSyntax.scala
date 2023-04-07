package ru.d4team.chat.utils

import zio.http.Body
import zio.json.{JsonDecoder, _}
import zio.{Task, ZIO}

trait BodyExtractorSyntax {
  implicit class BodyOps(body: Body) {
    def extractTo[B: JsonDecoder]: Task[B] = for {
      rawBody   <- body.asString
      extracted <- ZIO.fromEither(rawBody.fromJson[B].left.map(err => new Throwable(err)))
    } yield extracted
  }
}
