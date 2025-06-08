package s3j.internal.scala3_2_0

import scala.quoted.Quotes
import scala.quoted.runtime.impl.QuotesImpl

// noinspection DuplicatedCode
private object ClassGeneratorOpsImpl extends ClassGeneratorOps {
  override def newClass(using q: Quotes)(owner: q.reflect.Symbol, name: String): q.reflect.Symbol = {
    val qi: QuotesImpl with q.type = q.asInstanceOf[q.type & QuotesImpl]
    import qi.reflect.*
    Symbol.newClass(owner, name, List(TypeRepr.of[AnyRef]), _ => Nil, None)
  }

  override def classDef(using q: Quotes)(sym: q.reflect.Symbol, inner: List[q.reflect.Statement]): q.reflect.ClassDef = {
    val qi: QuotesImpl with q.type = q.asInstanceOf[q.type & QuotesImpl]
    import qi.reflect.*
    ClassDef(sym, List(TypeTree.of[AnyRef]), inner)
  }

  extension (using q: Quotes)(sym: q.reflect.Symbol) {
    override def enter(entry: q.reflect.Symbol): Unit = {
      val qi: QuotesImpl with q.type = q.asInstanceOf[q.type & QuotesImpl]

      import dotty.tools.dotc.core.Symbols
      import qi.ctx

      val cs: Symbols.ClassSymbol = sym.asInstanceOf[Symbols.Symbol].asClass
      cs.enter(entry.asInstanceOf[Symbols.Symbol])
    }
  }
}
