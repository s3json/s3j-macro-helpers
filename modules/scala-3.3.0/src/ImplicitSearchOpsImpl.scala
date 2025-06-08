package s3j.internal.scala3_3_0

import dotty.tools.dotc.ast.tpd
import dotty.tools.dotc.core.Contexts
import dotty.tools.dotc.core.Contexts.{Context, FreshContext}
import dotty.tools.dotc.core.Types.Type
import dotty.tools.dotc.typer.Implicits
import dotty.tools.dotc.typer.Implicits.ContextualImplicits
import dotty.tools.dotc.util.Spans.Span
import s3j.internal.scala3_2_0.ImplicitSearchOps

object ImplicitSearchOpsImpl extends ImplicitSearchOps {
  override def noContext: Context = Contexts.NoContext

  class ImplicitContext(
    outer: Context,
    override val implicits: ContextualImplicits
  ) extends FreshContext(outer.base) {
    reuseIn(outer)
    setTyperState(outer.typerState)
  }

  extension (c: Context) {
    override def freshWithImplicits(implicits: Implicits.ContextualImplicits): Context =
      ImplicitContext(c, implicits)

    override def inferImplicit(tpe: Type, span: Span): tpd.Tree =
      c.typer.inferImplicitArg(tpe, span)(using c)
  }
}
