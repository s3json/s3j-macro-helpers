package s3j.internal.macros

import scala.quoted.Quotes

private[s3j] object ForbiddenMacroUtils {
  /** Lookup an instance for the current compiler version */
  val instance: ForbiddenMacroUtils = CompilerBridge.current.forbiddenUtils
}

private[s3j] trait ForbiddenMacroUtils {
  /**
   * Workaround for dotty bug #16147
   *
   * @see https://github.com/lampepfl/dotty/issues/16147
   */
  def clearQuotesCache()(using Quotes): Unit

  /**
   * Quotes.XmacroSettings is still experimental for some reason — apparently, the experiments have been so thrilling
   * they just can't bring themselves to stop.
   *
   * Also, unlike the original XmacroSettings method, this method is intended to just pull the configuration from SBT,
   * without all this transparent/non-transparent mumble-jumble.
   */
  def macroSettings(using q: Quotes): Seq[String]

  /** Like `tpe.dealias`, but does not strip off annotations */
  def dealiasKeepAnnots(using q: Quotes)(tpe: q.reflect.TypeRepr): q.reflect.TypeRepr
}
