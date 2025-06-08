package s3j.test

import scala.quoted.{Expr, Quotes, Type}

object ImplicitHelpers {
  object Assisted {
    given assistedMeow[T]: CanMeow[T] = ???
//    inline given assistedMeow[T]: CanMeow[T] = ${ assistedMeowImpl[T] }
//
//    private def assistedMeowImpl[T](using Quotes, Type[T]): Expr[CanMeow[T]] = {
//      '{
//        new CanMeow[T] {
//          override def meow: String = ${ Expr("assisted:" + Type.show[T]) }
//        }
//      }
//    }
  }

  object ExtraLocation {
    given barCanMeow: CanMeow[Bar] with {
      override def meow: String = "bar"
    }
  }
}
