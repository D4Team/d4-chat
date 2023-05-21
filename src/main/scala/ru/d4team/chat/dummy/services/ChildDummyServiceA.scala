package ru.d4team.chat.dummy.services

import ru.d4team.chat.dummy.models.Dummy
import zio._

trait ChildDummyServiceA {
  def change(dummy: Dummy): UIO[Dummy]
}

object ChildDummyServiceA {
  val live: URLayer[Any, ChildDummyServiceA] = ZLayer.fromFunction(ChildDummyServiceAImpl.apply _)
}

final case class ChildDummyServiceAImpl() extends ChildDummyServiceA {
  override def change(dummy: Dummy): UIO[Dummy] = ZIO.succeed(dummy.copy(name = dummy.name.toUpperCase))
}
