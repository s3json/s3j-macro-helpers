import java.nio.file.{Files, StandardCopyOption}
import scala.xml.{Node, Elem}

ThisBuild / version := "0.1"
ThisBuild / scalaVersion := "3.2.0"

lazy val root = (project in file("."))
  .settings(
    organization := "io.s3j",
    name := "s3j-macro-helpers",

    libraryDependencies ++= Seq(
      "org.scala-lang" %% "scala3-compiler" % scalaVersion.value % Provided,
    ),

    publishMavenStyle := true,

    versionScheme := Some("semver-spec"),
    licenses := Seq("Apache 2" -> url("https://www.apache.org/licenses/LICENSE-2.0.txt")),

    pomPostProcess := { pom =>
      def filterDep(dep: Node): Boolean = dep match {
        case elem: Elem =>
          val groupId = (elem \ "groupId").text
          val artifactId = (elem \ "artifactId").text
          groupId != "org.scala-lang" || !artifactId.startsWith("scala3-compiler")

        case _ => false
      }

      def transform(node: Node, path: List[String]): Node = node match {
        case elem: Elem if elem.label == "dependencies" && path == "project" :: Nil =>
          elem.copy(child = elem.child.filter(filterDep))

        case elem: Elem =>
          elem.copy(child = elem.child.map(c => transform(c, elem.label :: path)))

        case _ => node
      }

      transform(pom, Nil)
    },
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
