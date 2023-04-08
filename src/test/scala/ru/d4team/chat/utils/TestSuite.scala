package ru.d4team.chat.utils

import zio.ZIO

trait TestSuite {
  implicit class EffectOps[R, Err, Out](zio: ZIO[R, Option[Err], Out]) {
    // method will fail if the effect will succeed
    def expectFailure: ZIO[R, String, Err] = zio.foldZIO(
      {
        case Some(err) => ZIO.succeed(err)
        case None      => ZIO.fail("Expected to get error, but not")
      },
      _ => ZIO.fail("Expected to get error, but not")
    )
  }
}
