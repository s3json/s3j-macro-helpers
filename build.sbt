ThisBuild / organization := "io.s3j"
ThisBuild / version := "0.2"
ThisBuild / scalaVersion := "3.2.0"

Global / excludeLintKeys := Set(idePackagePrefix, commands)

// Dependencies in this configuration are not included in POM document
val Hidden = config("Hidden").hide.withExtendsConfigs(Vector(Compile))
val commonConfig = Seq(
  ivyConfigurations += Hidden,
  Compile / classpathConfiguration := Hidden,

  Compile / scalaSource := baseDirectory.value / "src",
  Compile / javaSource := baseDirectory.value / "src-java",
  Compile / resourceDirectory := baseDirectory.value / "resources",
  Test / scalaSource := baseDirectory.value / "test-src",
  Test / javaSource := baseDirectory.value / "test-src-java",
  Test / resourceDirectory := baseDirectory.value / "test-resources",
)

val scalaVersionHelperConfig = commonConfig ++ Seq(
  name := "impl-scala-" + scalaVersion.value,
  idePackagePrefix := Some("s3j.internal.scala" + scalaVersion.value.replace('.', '_')),

  libraryDependencies ++= Seq(
    "org.scala-lang" %% "scala3-compiler" % scalaVersion.value % Hidden,
  )
)

val aggregatedProjects = Seq(
  api,
  scala_3_2_0,
  scala_3_3_0,
  scala_3_7_0,
)

lazy val root = (project in file("."))
  .settings(commonConfig *)
  .settings(
    name := "s3j-macro-helpers",

    publishMavenStyle := true,
    versionScheme := Some("semver-spec"),
    licenses := Seq("Apache 2" -> url("https://www.apache.org/licenses/LICENSE-2.0.txt")),

    Compile / packageBin := Packager.combineJars(
      target.value / ("s3j-macro-helpers_3-" + version.value + ".jar"),
      aggregatedProjects.map(p => p / Compile / packageBin).join.value
    ),

    Compile / packageSrc := (api / Compile / packageSrc).value,
    Compile / packageDoc := (api / Compile / packageDoc).value,
  )
  .dependsOn(aggregatedProjects.map(_ % Hidden) *)

lazy val api = (project in file("modules/api"))
  .settings(commonConfig *)

lazy val scala_3_2_0 = (project in file("modules/scala-3.2.0"))
  .dependsOn(api)
  .settings(scalaVersionHelperConfig *)
  .settings(scalaVersion := "3.2.0")

lazy val scala_3_3_0 = (project in file("modules/scala-3.3.0"))
  .dependsOn(api, scala_3_2_0)
  .settings(scalaVersionHelperConfig *)
  .settings(scalaVersion := "3.3.0")

lazy val scala_3_7_0 = (project in file("modules/scala-3.7.0"))
  .dependsOn(api, scala_3_3_0)
  .settings(scalaVersionHelperConfig *)
  .settings(scalaVersion := "3.7.0")

commands += Packager.prepareTestCommand(root)
