package ru.d4team.chat.dummy.services

import ru.d4team.chat.dummy.models.{Dummy, DummyResult}
import zio._

trait ChildDummyServiceB {
  def add(dummy: Dummy): UIO[DummyResult]
}

object ChildDummyServiceB {
  val live: URLayer[Any, ChildDummyServiceB] = ZLayer.fromFunction(ChildDummyServiceBImpl.apply _)
}

final case class ChildDummyServiceBImpl() extends ChildDummyServiceB {
  override def add(dummy: Dummy): UIO[DummyResult] = ZIO.succeed(DummyResult(dummy.name.count(_.isUpper)))
}
