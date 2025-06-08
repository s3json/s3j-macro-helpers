package s3j.internal.macros

import scala.math.Ordering.Implicits.infixOrderingOps
import scala.quoted.Quotes
import scala.util.control.NonFatal

private[s3j] object CompilerBridge {
  private type VersionTuple = (Int, Int, Int)
  private val versionClasses: Array[(VersionTuple, String)] = Array(
    (3, 2, 0) -> "s3j.internal.scala3_2_0.CompilerBridgeImpl",
    (3, 3, 0) -> "s3j.internal.scala3_3_0.CompilerBridgeImpl",
    (3, 7, 0) -> "s3j.internal.scala3_7_0.CompilerBridgeImpl",
  )

  /** Compiler bridge for the current version of the compiler */
  val current: CompilerBridge = {
    val currentVersion = CompilerVersion.current()

    val (_, className) = versionClasses
      .filter { case ((major, minor, patch), _) => currentVersion >= CompilerVersion(major, minor, patch) }
      .lastOption
      .getOrElse(throw new RuntimeException(s"Unsupported Dotty compiler version (too low): $currentVersion"))

    try Class.forName(className).getConstructor().newInstance().asInstanceOf[CompilerBridge]
    catch {
      case NonFatal(e) => throw new RuntimeException("Failed to instantiate compiler bridge class: " + className, e)
    }
  }
}

private[s3j] trait CompilerBridge {
  /** @return Instance for the [[ForbiddenMacroUtils]] helper */
  def forbiddenUtils: ForbiddenMacroUtils

  /** @return Instance for the [[ExtendedImplicitSearch]] helper */
  def extendedImplicitSearch: ExtendedImplicitSearch

  /** @return New instance of the class generator */
  def classGenerator(className: String)(using q: Quotes): ClassGenerator
}
