package s3j.internal.macros

import java.util.Properties
import scala.util.control.NonFatal

private[s3j] object CompilerVersion {
  /** Read and parse compiler version from the current environment */
  def current(): CompilerVersion = {
    val props: Properties =
      try {
        val result = Properties()
        val propsClass = Class.forName("dotty.tools.dotc.config.PropertiesTrait")
        val stream = propsClass.getResourceAsStream("/compiler.properties")
        if (stream == null) {
          throw new RuntimeException("Failed to determine Dotty compiler version: 'compiler.properties' file does " +
            "not exists in the classpath of " + propsClass)
        }

        try result.load(stream)
        finally stream.close()

        result
      } catch {
        case cnf: ClassNotFoundException =>
          throw new RuntimeException("Failed to determine Dotty compiler version", cnf)
      }

    val version = props.getProperty("maven.version.number")
    if (version == null) {
      throw new RuntimeException("Failed to determine Dotty compiler version: property does not exists")
    }

    try {
      val versionNumber = version.split("-").head
      versionNumber.split("\\.").take(3) match {
        case Array(major, minor, patch) => CompilerVersion(major.toInt, minor.toInt, patch.toInt)
        case Array(major, minor) => CompilerVersion(major.toInt, minor.toInt, 0)
        case _ => throw new IllegalArgumentException(s"Malformed version number format: $versionNumber")
      }
    } catch {
      case NonFatal(e) => throw new RuntimeException(s"Failed to parse compiler version: $version", e)
    }
  }
}

private[s3j] case class CompilerVersion(major: Int, minor: Int, patch: Int) extends Comparable[CompilerVersion] {
  override def compareTo(o: CompilerVersion): Int =
    if (major != o.major) major.compareTo(o.major)
    else if (minor != o.minor) minor.compareTo(o.minor)
    else patch.compareTo(o.patch)

  override def toString: String = s"$major.$minor.$patch"
}
