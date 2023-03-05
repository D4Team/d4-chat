package ru.d4team.chat.dummy.services

import ru.d4team.chat.dummy.models.{Dummy, DummyResult}
import zio.{UIO, URLayer, ZLayer}

object MainDummyService {

  type Env = ChildDummyServiceA with ChildDummyServiceB

  val live: URLayer[Env, MainDummyServiceImpl] = ZLayer.fromFunction(MainDummyServiceImpl.apply _)
}

trait MainDummyService {
  def add(dummy: Dummy): UIO[DummyResult]
}

final case class MainDummyServiceImpl(serviceA: ChildDummyServiceA, serviceB: ChildDummyServiceB)
    extends MainDummyService {
  override def add(dummy: Dummy): UIO[DummyResult] = for {
    changed <- serviceA.change(dummy)
    result  <- serviceB.add(changed)
  } yield result
}
