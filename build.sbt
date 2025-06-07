import java.nio.file.{Files, StandardCopyOption}

ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.2.0"

lazy val root = (project in file("."))
  .settings(
    name := "s3j-macro-helpers",
    
    libraryDependencies ++= Seq(
      "org.scala-lang" %% "scala3-compiler" % scalaVersion.value % Provided,
    )
  )

commands += Command.command("prepareTest") { st =>
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
