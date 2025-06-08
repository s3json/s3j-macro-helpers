package s3j.internal.scala3_2_0

import scala.quoted.Quotes
import scala.quoted.runtime.impl.QuotesImpl

// everything else in ClassGenerator is a public stable API
private[internal] trait ClassGeneratorOps {
  def newClass(using q: Quotes)(owner: q.reflect.Symbol, name: String): q.reflect.Symbol

  def classDef(using q: Quotes)(sym: q.reflect.Symbol, inner: List[q.reflect.Statement]): q.reflect.ClassDef

  extension (using q: Quotes)(sym: q.reflect.Symbol) {
    def enter(entry: q.reflect.Symbol): Unit
  }
}
