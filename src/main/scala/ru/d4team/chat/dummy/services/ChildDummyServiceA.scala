package ru.d4team.chat.dummy.services

import ru.d4team.chat.dummy.models.Dummy
import zio.UIO

trait ChildDummyServiceA {
  def change(dummy: Dummy): UIO[Dummy]
}
