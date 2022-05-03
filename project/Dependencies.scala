import sbt._

object Dependencies {
  lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.2.11"
  lazy val fs2Kafka = "com.github.fd4s" %% "fs2-kafka" % "2.5.0-M3"
  lazy val fs2KafaAvro = "com.github.fd4s" %% "fs2-kafka-vulcan" % "2.5.0-M3"

  lazy val http4sLibs: Seq[ModuleID] = {
    val http4sVersion = "0.23.11"
    Seq(
      "org.http4s" %% "http4s-dsl" % http4sVersion,
      "org.http4s" %% "http4s-ember-server" % http4sVersion,
      "org.http4s" %% "http4s-ember-client" % http4sVersion,
      "org.http4s" %% "http4s-circe" % http4sVersion,
      "org.scalameta"   %% "munit"               % "0.7.29"           % Test,
      "org.typelevel"   %% "munit-cats-effect-3" % "1.0.7" % Test,
      "ch.qos.logback"  %  "logback-classic"     % "1.2.10"       % Runtime
    )
  }

  lazy val circeLibs: Seq[ModuleID] = {
    val circeVersion = "0.14.1"
    Seq(
      "io.circe" %% "circe-core",
      "io.circe" %% "circe-generic",
      "io.circe" %% "circe-parser"
    ).map(_ % circeVersion)
  }

  lazy val pureConfigLibs: Seq[ModuleID] = {
    val pureVersion = "0.17.1"
      Seq(
        "com.github.pureconfig" %% "pureconfig-cats-effect" % pureVersion,
        "com.github.pureconfig" %% "pureconfig" % pureVersion,
        "com.github.pureconfig" %% "pureconfig-http4s" % pureVersion,
        "com.github.pureconfig" %% "pureconfig-generic" % pureVersion

      )
  }
}
