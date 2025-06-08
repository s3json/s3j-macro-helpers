package s3j.internal.scala3_2_0

import dotty.tools.dotc.ast.tpd
import dotty.tools.dotc.core.Contexts.{Context, NoContext}
import dotty.tools.dotc.core.Types.Type
import dotty.tools.dotc.typer.Implicits.ContextualImplicits
import dotty.tools.dotc.util.Spans.Span

private object ImplicitSearchOpsImpl extends ImplicitSearchOps {
  override def noContext: Context = NoContext

  extension (c: Context) {
    override def freshWithImplicits(implicits: ContextualImplicits): Context =
      c.fresh.setImplicits(implicits)

    override def inferImplicit(tpe: Type, span: Span): tpd.Tree =
      c.typer.inferImplicitArg(tpe, span)(using c)
  }
}
