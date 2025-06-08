package s3j.test

import s3j.internal.macros.{ClassGenerator, ExtendedImplicitSearch, ForbiddenMacroUtils}

import scala.quoted.{Expr, Quotes, Type}

object Macros {
  inline def flushQuotesCache(): Unit = ${ flushQuotesImpl }
  inline def testClassGenerator(): Int = ${ classGeneratorImpl }
  inline def macroSettings(): Seq[String] = ${ macroSettingsImpl }
  inline def dealias[T]: String = ${ dealiasImpl[T] }
  inline def findImplicit[T]: String = ${ findImplicitImpl[T] }

  private def flushQuotesImpl(using Quotes): Expr[Unit] = {
    ForbiddenMacroUtils.instance.clearQuotesCache()
    '{ () }
  }

  private def classGeneratorImpl(using q: Quotes): Expr[Int] = {
    import q.reflect.{*, given}
    val gen = ClassGenerator("meow")

    val a = gen.defineField[Int]("a")
    val b = gen.defineField[Int]("b")
    val c = gen.defineField[Int]("c")

    a.setInitializer('{ 1 + ${ b.reference } })
    b.setInitializer('{ 2 })
    c.setInitializer('{ 3 + ${ a.reference } })

    a.setPosition(1)
    c.setPosition(2)

    gen.build(c.externalReference)
  }

  private def macroSettingsImpl(using q: Quotes): Expr[Seq[String]] = {
    Expr(ForbiddenMacroUtils.instance.macroSettings)
  }

  private def dealiasImpl[T](using q: Quotes, tt: Type[T]): Expr[String] = {
    import q.reflect.*
    Expr(ForbiddenMacroUtils.instance.dealiasKeepAnnots(TypeRepr.of[T]).show)
  }

  private def findImplicitImpl[T](using q: Quotes, tt: Type[T]): Expr[String] = {
    import q.reflect.{*, given}

    val result = ExtendedImplicitSearch.instance.search(
      TypeRepr.of[T], Position.ofMacroExpansion,
      List(
        Symbol.requiredModule("s3j.test.ImplicitHelpers.Assisted"),
        Symbol.requiredModule("s3j.test.ImplicitHelpers.ExtraLocation"),
      )
    )

    result match {
      case r: ImplicitSearchSuccess => Expr(r.tree.show)
      case r: ImplicitSearchFailure => Expr("failure: " + r.explanation)
    }
  }
}
