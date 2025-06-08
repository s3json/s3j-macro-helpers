package s3j.internal.scala3_7_0

import s3j.internal.scala3_2_0.ImplicitSearchOps
import s3j.internal.scala3_3_0.CompilerBridgeImpl as CompilerBridge_3_3_0

private[internal] class CompilerBridgeImpl extends CompilerBridge_3_3_0 {
  override protected def implicitSearchOps: ImplicitSearchOps = ImplicitSearchOpsImpl
}
