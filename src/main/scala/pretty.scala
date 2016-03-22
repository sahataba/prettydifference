package pretty

import java.time.LocalDate

import shapeless._

case class Address(street:String, city:String)
case class User(name:String, lastName:String, birthDate:LocalDate, address:Address)

object CaseClassDiff  extends App{
  private implicit def fooIso = Generic[User]

  private def diff[H1 <: HList, H2 <: HList](lst1: H1, lst2: H2)(implicit e: H1 =:= H2): List[String] = (lst1, lst2) match {
    case (HNil, HNil)                 => List()
    case (h1::t1, h2::t2) if h1 != h2 => s"$h1 -> $h2" :: diff(t1, t2)
    case (h1::t1, h2::t2)             => diff(t1, t2)
    case _                            => throw new RuntimeException("something went very wrong")
  }

  def difference(value1:User, value2:User): List[String] = {
    val v1 = fooIso.to(value1)
    val v2 = fooIso.to(value2)
    diff(v1, v2)
  }

  println(s"Difference ${difference(User("John", "Doe", LocalDate.of(1983,5,12), Address("Mainstreet 5", "Metropolis")), User("Jane", "Doe", LocalDate.of(1983,3,30), Address("Mainstreet 6", "Metropolis")))}")

}
