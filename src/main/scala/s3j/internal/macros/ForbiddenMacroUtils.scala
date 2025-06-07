package s3j.internal.macros

import dotty.tools.dotc.quoted.QuotesCache
import dotty.tools.dotc.util.Property

import java.lang.reflect.Field
import scala.annotation.tailrec
import scala.quoted.runtime.impl.QuotesImpl
import scala.quoted.{Quotes, quotes}

private[s3j] object ForbiddenMacroUtils {
  private def getStaticField(cls: Class[_], fieldName: String): AnyRef = {
    val f = cls.getDeclaredField(fieldName)
    f.setAccessible(true)
    f.get(null)
  }

  /**
   * `XmacroSettings` is experimental for some reason (say hello to those compiler guys!) and I don't want that
   * experimentalism to recursively contaminate my code - after all, reading settings is not the worst thing that I've
   * done with the Scala compiler in this project
   */
  def macroSettings(using q: Quotes): Seq[String] = {
    val ctx = q.asInstanceOf[QuotesImpl].ctx
    ctx.settings.XmacroSettings.valueIn(ctx.settingsState)
  }

  /**
   * Workaround for dotty bug #16147
   *
   * @see https://github.com/lampepfl/dotty/issues/16147
   */
  def clearQuotesCache()(using Quotes): Unit = {
    val context = quotes.asInstanceOf[QuotesImpl].ctx
    val cacheKey = getStaticField(QuotesCache.getClass, "QuotesCacheKey")
      .asInstanceOf[Property.Key[scala.collection.mutable.Map[_, _]]]

    context.property(cacheKey).foreach(_.clear())
  }

  /** Like `tpe.dealias`, but does not strip off annotations */
  def dealiasKeepAnnots(using q: Quotes)(tpe: q.reflect.TypeRepr): q.reflect.TypeRepr = {
    val qi: q.type & QuotesImpl = q.asInstanceOf[q.type & QuotesImpl]
    import qi.ctx
    tpe
      .asInstanceOf[qi.reflect.TypeRepr]
      .dealiasKeepAnnots
      .asInstanceOf[q.reflect.TypeRepr]
  }
}
