ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.8"

val http4sVersion = "1.0.0-M37"

libraryDependencies ++= Seq(
  "org.http4s" %% "http4s-dsl" % http4sVersion,
  "org.http4s" %% "http4s-ember-server" % http4sVersion,
  "org.http4s" %% "http4s-ember-client" % http4sVersion,
  "ch.qos.logback" % "logback-classic" % "1.4.4" % Runtime,
  "org.reactivemongo" %% "reactivemongo" % "1.1.0-RC5",
  "org.reactivemongo" % "reactivemongo-shaded-native" % "1.1.0-RC5-osx-x86-64" % Runtime,
  "org.http4s" %% "http4s-circe" % http4sVersion,
  // Optional for auto-derivation of JSON codecs
  "io.circe" %% "circe-generic" % "0.14.3",
  // Optional for string interpolation to JSON model
  "io.circe" %% "circe-literal" % "0.14.3",
  "io.circe" %% "circe-fs2" % "0.14.0",
  "org.scalatest" %% "scalatest" % "3.2.14" % Test,
  "com.dimafeng" %% "testcontainers-scala-scalatest" % "0.40.11" % Test,
  "com.dimafeng" %% "testcontainers-scala-mongodb" % "0.40.11" % Test
)