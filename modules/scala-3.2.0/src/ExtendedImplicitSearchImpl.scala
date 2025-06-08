package s3j.internal.scala3_2_0

import dotty.tools.dotc.ast.{tpd, untpd}
import dotty.tools.dotc.core.Contexts.{Context, NoContext, ctx, inContext}
import dotty.tools.dotc.core.{StdNames, Symbols, Types}
import dotty.tools.dotc.typer.Implicits.{ContextualImplicits, ImplicitRefs}
import dotty.tools.dotc.typer.ImportInfo
import dotty.tools.dotc.util.{SourcePosition, Spans}
import s3j.internal.macros.ExtendedImplicitSearch

import scala.quoted.Quotes
import scala.quoted.runtime.impl.QuotesImpl

private class ExtendedImplicitSearchImpl(using ops: ImplicitSearchOps) extends ExtendedImplicitSearch {
  override def search(using q: Quotes)(
    targetType: q.reflect.TypeRepr,
    position: q.reflect.Position,
    additionalLocations: Seq[q.reflect.Symbol]
  ): q.reflect.ImplicitSearchResult = {
    val qi: q.type & QuotesImpl = q.asInstanceOf[QuotesImpl & q.type]
    import q.reflect.{*, given}
    import qi.ctx as macroCtx

    def importImplicits(sym: Symbol, outer: ContextualImplicits)(using Context): ContextualImplicits = {
      val importInfo = new ImportInfo(
        Symbols.newImportSymbol(ctx.owner, tpd.Ident(sym.asInstanceOf[Symbols.Symbol].namedType), Spans.NoCoord),
        List(untpd.ImportSelector(untpd.Ident(StdNames.nme.EMPTY))),
        untpd.EmptyTree, isRootImport = false
      )

      inContext(ctx.fresh.setOwner(importInfo.importSym)) {
        ContextualImplicits(importInfo.importedImplicits, outer, isImport = true)(ctx)
      }
    }

    extension (c: ImplicitRefs) {
      def effectiveContext: Context =
        if (c eq ops.noContext.implicits) macroCtx
        else c.irefCtx
    }

    def transplant(c: ImplicitRefs, outer: ContextualImplicits): ContextualImplicits =
      inContext(c.effectiveContext.freshOver(outer.effectiveContext)) {
        new ContextualImplicits(c.refs, outer, ctx.owner.isImport)(ctx)
      }

    val additionalImplicits: ContextualImplicits =
      additionalLocations.foldLeft(ops.noContext.implicits) { (outer, s) =>
        inContext(outer.effectiveContext) { importImplicits(s, outer) }
      }

    // Type implicits (those coming from the companion object without any imports) are implemented as a special case
    // in the compiler, as they are queried only if contextual implicits (those brought by imports) yield a failure.
    // In the assisted implicits case, this would never happen - we have a catch-all entry at the lowest level.
    // Therefore, we need to re-introduce type implicits to our stack so they will have a chance to be found:
    val typeImplicits: ContextualImplicits = {
      val c = ctx.run.implicitScope(targetType.asInstanceOf[Types.Type])
      transplant(c, additionalImplicits)
    }

    def transplantStack(x: ContextualImplicits): ContextualImplicits =
      if (x eq ops.noContext.implicits) typeImplicits
      else transplant(x, transplantStack(x.outerImplicits))

    val finalImplicits: ContextualImplicits = transplantStack(ctx.implicits)
    val finalContext = finalImplicits.effectiveContext.freshWithImplicits(finalImplicits)

    inContext(finalContext) {
      finalContext.inferImplicit(
        targetType.asInstanceOf[Types.Type],
        position.asInstanceOf[SourcePosition].span
      ).asInstanceOf[q.reflect.ImplicitSearchResult]
    }
  }
}
