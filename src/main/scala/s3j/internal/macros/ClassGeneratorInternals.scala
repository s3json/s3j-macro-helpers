package s3j.internal.macros

import scala.quoted.Quotes
import scala.quoted.runtime.impl.QuotesImpl

// noinspection DuplicatedCode
private object ClassGeneratorInternals extends ClassGeneratorInternals {
  override def newClass(using q: Quotes)(owner: q.reflect.Symbol, name: String): q.reflect.Symbol = {
    val qi: QuotesImpl with q.type = q.asInstanceOf[q.type & QuotesImpl]
    import qi.reflect.{*, given}
    Symbol.newClass(owner, name, List(TypeRepr.of[AnyRef]), _ => Nil, None)
  }

  override def classDef(using q: Quotes)(sym: q.reflect.Symbol, inner: List[q.reflect.Statement]): q.reflect.ClassDef = {
    val qi: QuotesImpl with q.type = q.asInstanceOf[q.type & QuotesImpl]
    import qi.reflect.{*, given}
    ClassDef(sym, List(TypeTree.of[AnyRef]), inner)
  }

  extension (using q: Quotes)(sym: q.reflect.Symbol) {
    override def enter(entry: q.reflect.Symbol): Unit = {
      val qi: QuotesImpl with q.type = q.asInstanceOf[q.type & QuotesImpl]

      import dotty.tools.dotc.core.Symbols
      import qi.reflect.{*, given}
      import qi.ctx

      val cs: Symbols.ClassSymbol = sym.asInstanceOf[Symbols.Symbol].asClass
      cs.enter(entry.asInstanceOf[Symbols.Symbol])
    }
  }
}

// everything else in ClassGenerator is a public stable API
private trait ClassGeneratorInternals {
  def newClass(using q: Quotes)(owner: q.reflect.Symbol, name: String): q.reflect.Symbol

  def classDef(using q: Quotes)(sym: q.reflect.Symbol, inner: List[q.reflect.Statement]): q.reflect.ClassDef

  extension (using q: Quotes)(sym: q.reflect.Symbol) {
    def enter(entry: q.reflect.Symbol): Unit
  }
}
