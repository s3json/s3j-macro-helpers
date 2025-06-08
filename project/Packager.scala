import sbt.Keys.*
import sbt.*

import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, StandardCopyOption}
import java.util.zip.{ZipEntry, ZipInputStream, ZipOutputStream}

object Packager {
  def combineJars(out: File, jars: Seq[File]): File = {
    val sOut = Files.newOutputStream(out.toPath)
    try {
      val zos = new ZipOutputStream(sOut, StandardCharsets.UTF_8)

      for ((jar, i) <- jars.zipWithIndex) {
        val sIn = Files.newInputStream(jar.toPath)
        try {
          val zis = new ZipInputStream(sIn, StandardCharsets.UTF_8)
          for (entry <- Iterator.continually(zis.getNextEntry).takeWhile(_ != null)) {
            val keep = !entry.isDirectory && (i == 0 || !entry.getName.startsWith("META-INF/"))

            if (keep) {
              zos.putNextEntry(new ZipEntry(entry.getName))
              zis.transferTo(zos)
              zos.closeEntry()
            }
          }
        } finally sIn.close()
      }

      zos.close()
    } finally sOut.close()

    out
  }

  def prepareTestCommand(root: Project) = Command.command("prepareTest") { st =>
    val extracted = Project.extract(st)
    val (nst, ret) = extracted.runTask(root / Compile / packageBin, st)
    val baseDir = st.getSetting(baseDirectory).get

    val libDir = baseDir / "test" / "lib"
    val copyDst = libDir / "helpers-build.jar"

    val log = st.getSetting(sLog).get
    log.info("Copying built JAR file:")
    log.info(" from: " + ret.getAbsolutePath)
    log.info(" to:   " + copyDst.getAbsolutePath)

    if (!libDir.exists()) {
      Files.createDirectories(libDir.toPath)
    }

    Files.copy(ret.toPath, copyDst.toPath, StandardCopyOption.REPLACE_EXISTING)

    nst
  }

}
