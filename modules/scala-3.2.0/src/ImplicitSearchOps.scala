package s3j.internal.scala3_2_0

import dotty.tools.dotc.ast.tpd
import dotty.tools.dotc.core.Contexts.{Context, NoContext}
import dotty.tools.dotc.core.Types.Type
import dotty.tools.dotc.typer.Implicits.ContextualImplicits
import dotty.tools.dotc.util.Spans.Span

private[internal] trait ImplicitSearchOps {
  def noContext: Context

  extension (c: Context) {
    def freshWithImplicits(implicits: ContextualImplicits): Context
    def inferImplicit(tpe: Type, span: Span): tpd.Tree
  }
}
