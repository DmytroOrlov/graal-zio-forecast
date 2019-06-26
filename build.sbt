import Dependencies._

lazy val zioVersion = "1.0.0-RC8-12"

lazy val `scala-with-cats` = (project in file(".")).
  settings(
    inThisBuild(Seq(
      scalaVersion := "2.12.8",
      version := "0.1.0-SNAPSHOT",
      organization := "com.github.DmytroOrlov"
    )),
    addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.10.3"),
    addCompilerPlugin(("org.scalameta" % "paradise" % "3.0.0-M11").cross(CrossVersion.full)),
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio" % zioVersion,
      "dev.zio" %% "zio-interop-cats" % zioVersion,
      "org.typelevel" %% "cats-core" % "2.0.0-M4",
      "org.typelevel" %% "cats-mtl-core" % "0.5.0",
      "org.typelevel" %% "cats-tagless-macros" % "0.8",
      "io.monix" %% "monix" % "3.0.0-RC3",
      scalaTest % Test,
      scalaCheck % Test
    ),
    scalacOptions ++= Seq(
      "-Ypartial-unification"
      //, "-Xfatal-warnings"
    )
  )
