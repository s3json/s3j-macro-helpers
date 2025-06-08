package s3j.internal.scala3_2_0

import s3j.internal.macros.{ClassGenerator, CompilerBridge, ExtendedImplicitSearch, ForbiddenMacroUtils}

import scala.quoted.Quotes

private[internal] class CompilerBridgeImpl extends CompilerBridge {
  override def forbiddenUtils: ForbiddenMacroUtils = ForbiddenMacroUtilsImpl
  override def extendedImplicitSearch: ExtendedImplicitSearch = ExtendedImplicitSearchImpl(using implicitSearchOps)

  override def classGenerator(className: String)(using q: Quotes): ClassGenerator =
    new ClassGeneratorImpl(className)(using q, classGeneratorOps)

  protected def classGeneratorOps: ClassGeneratorOps = ClassGeneratorOpsImpl
  protected def implicitSearchOps: ImplicitSearchOps = ImplicitSearchOpsImpl
}
