val root = project
  .in(file("."))
  .settings(
    scalaVersion := "2.13.5",
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-effect-std" % "3.0.0-RC2",
      "org.typelevel" %% "cats-effect" % "3.0.0-RC2" % Test,
      "io.projectreactor" % "reactor-core" % "3.4.4"
    )
  )
