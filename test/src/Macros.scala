package s3j.test

import s3j.internal.macros.{ClassGenerator, ForbiddenMacroUtils}

import scala.quoted.{Expr, Quotes, Type}

object Macros {
  inline def flushQuotesCache(): Unit = ${ flushQuotesImpl }
  inline def testClassGenerator(): Int = ${ classGeneratorImpl }
  inline def macroSettings(): Seq[String] = ${ macroSettingsImpl }
  inline def dealias[T]: String = ${ dealiasImpl[T] }
  
  private def flushQuotesImpl(using Quotes): Expr[Unit] = {
    ForbiddenMacroUtils.clearQuotesCache()
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
    Expr(ForbiddenMacroUtils.macroSettings)
  }
  
  private def dealiasImpl[T](using q: Quotes, tt: Type[T]): Expr[String] = {
    import q.reflect.*
    Expr(ForbiddenMacroUtils.dealiasKeepAnnots(TypeRepr.of[T]).show)
  }
}
