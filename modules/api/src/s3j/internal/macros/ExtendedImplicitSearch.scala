package s3j.internal.macros

import scala.quoted.Quotes

private[s3j] object ExtendedImplicitSearch {
  /** Lookup an instance for the current compiler version */
  val instance: ExtendedImplicitSearch = CompilerBridge.current.extendedImplicitSearch
}

private[s3j] trait ExtendedImplicitSearch {
  /**
   * Perform implicit search with additional imports injected into the current context.
   *
   * Symbols from `additionalLocations` get injected as wildcard imports at the lowest level, followed by any "normal"
   * implicits currently in scope. Additional locations are prioritized so that first entry in the list has the lowest
   * precedence.
   */
  def search(using q: Quotes)(
    targetType: q.reflect.TypeRepr,
    position: q.reflect.Position,
    additionalLocations: Seq[q.reflect.Symbol]
  ): q.reflect.ImplicitSearchResult
}
