package s3j.test

trait CanMeow[T] {
  def meow: String
}

object CanMeow {
  given fooCanMeow: CanMeow[Foo] with {
    def meow: String = "foo"
  }

  given iterableCanMeow[T, C <: Iterable[T]](using v: CanMeow[T]): CanMeow[C] with {
    override def meow: String = s"iterable(${v.meow})"
  }
}
