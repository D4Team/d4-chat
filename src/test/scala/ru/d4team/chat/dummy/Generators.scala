package ru.d4team.chat.dummy

import ru.d4team.chat.dummy.models.Dummy
import zio.test.Gen
import zio.test.magnolia._

object Generators {

  val genDummy: Gen[Any, Dummy] = DeriveGen[Dummy]

}
