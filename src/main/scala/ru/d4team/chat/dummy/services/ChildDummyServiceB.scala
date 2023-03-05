package ru.d4team.chat.dummy.services

import ru.d4team.chat.dummy.models.{Dummy, DummyResult}
import zio.UIO

trait ChildDummyServiceB {
  def add(dummy: Dummy): UIO[DummyResult]
}
