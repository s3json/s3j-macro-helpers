package s3j.internal.scala3_3_0

import s3j.internal.macros.ExtendedImplicitSearch
import s3j.internal.scala3_2_0.{ImplicitSearchOps, CompilerBridgeImpl as CompilerBridge_3_2_0}

private[internal] class CompilerBridgeImpl extends CompilerBridge_3_2_0 {
  override protected def implicitSearchOps: ImplicitSearchOps = ImplicitSearchOpsImpl
}
