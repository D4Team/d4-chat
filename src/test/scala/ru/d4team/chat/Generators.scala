package ru.d4team.chat

import ru.d4team.chat.models.person.Person
import zio.test.Gen
import zio.test.magnolia.DeriveGen

object Generators {
  val genPerson: Gen[Any, Person]           = DeriveGen[Person]
  val genPersonList: Gen[Any, List[Person]] = DeriveGen[List[Person]]
}
