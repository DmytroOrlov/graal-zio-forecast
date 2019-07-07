import java.nio.file.{Files, StandardCopyOption}

lazy val zioVersion = "1.0.0-RC9"

lazy val `scala-with-cats` = (project in file(".")).
  settings(
    inThisBuild(Seq(
      scalaVersion := "2.12.8",
      version := "0.1.0-SNAPSHOT",
      organization := "com.github.DmytroOrlov"
    )),
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio" % zioVersion,
      "org.typelevel" %% "cats-core" % "2.0.0-M4"
    ),
    scalacOptions ++= Seq(
      "-Ypartial-unification"
      //, "-Xfatal-warnings"
    )
  )
  .enablePlugins(DockerPlugin, JavaServerAppPackaging)
  .settings(
    dockerGraalvmNative := {
      val log = streams.value.log

      val stageDir = target.value / "native-docker" / "stage"
      stageDir.mkdirs()

      // copy all jars to the staging directory
      val cpDir = stageDir / "cp"
      cpDir.mkdirs()

      val classpathJars = Seq((packageBin in Compile).value) ++
        (dependencyClasspath in Compile).value.map(_.data)
      classpathJars.foreach(cpJar => Files.copy(
        cpJar.toPath,
        (cpDir / cpJar.name).toPath,
        StandardCopyOption.REPLACE_EXISTING))

      val resultDir = stageDir / "result"
      resultDir.mkdirs()
      val resultName = "out"

      val className = (mainClass in Compile).value
        .getOrElse(sys.error("Could not find a main class."))

      val runNativeImageCommand = Seq(
        "docker",
        "run",
        "--rm",
        "-v",
        s"${cpDir.getAbsolutePath}:/opt/cp",
        "-v",
        s"${resultDir.getAbsolutePath}:/opt/graalvm",
        "graalvm-native-image",
        "--initialize-at-build-time=scala.Function1",
//        "--report-unsupported-elements-at-runtime",
        "-cp",
        "/opt/cp/*",
        "--static",
        s"-H:Name=$resultName",
        className
      )

      log.info("Running native-image using the 'graalvm-native-image' docker container")
      log.info(s"Running: ${runNativeImageCommand.mkString(" ")}")

      sys.process.Process(runNativeImageCommand, resultDir) ! streams.value.log match {
        case 0 => resultDir / resultName
        case r => sys.error(s"Failed to run docker, exit status: " + r)
      }

      IO.write(
        file((stageDir / "Dockerfile").getAbsolutePath),
        """#FROM alpine:3.10.0
          |FROM alpine
          |COPY out /opt/docker/out
          |RUN chmod +x /opt/docker/out
          |CMD ["/opt/docker/out"]
          |""".stripMargin.getBytes("UTF-8")
      )
      val buildContainerCommand = Seq(
        "docker",
        "build",
        "-t",
        name.value,
        "-f",
        (stageDir / "Dockerfile")
          .getAbsolutePath,
        resultDir.absolutePath
      )

      log.info("Building the container with the generated native image")
      log.info(s"Running: ${buildContainerCommand.mkString(" ")}")

      sys.process.Process(buildContainerCommand, resultDir) ! streams.value.log match {
        case 0 => resultDir / resultName
        case r => sys.error(s"Failed to run docker, exit status: " + r)
      }
    }
  )

val dockerGraalvmNative = taskKey[Unit](
  "Create a docker image containing a binary build with GraalVM's native-image."
)
