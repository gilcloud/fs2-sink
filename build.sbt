import Dependencies._
import sbt.ThisBuild

lazy val commonSettings = Seq(
  addCompilerPlugin("org.typelevel" % "kind-projector" % "0.13.2" cross CrossVersion.full),
  addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1")
)

lazy val root = Project(id = "fs2-sink", base = file("."))
  .aggregate(sinkCore, sinkKafka, sinkSalesforce)
  .settings(publish / skip := true)

lazy val sinkCore = {
  (project in file("core"))
    .settings(commonSettings: _*)
    .settings(name := "fs2 sink core")
}

lazy val sinkKafka = {
  (project in file("kafka"))
    .settings(commonSettings: _*)
    .settings(
      name := "fs2 sink source kafka",
      resolvers += "confluent" at "https://packages.confluent.io/maven/",
      libraryDependencies += fs2Kafka,
      libraryDependencies += fs2KafaAvro
    )
    .dependsOn(sinkCore)
}

lazy val sinkSalesforce = {
  (project in file("salesforce"))
    .settings(commonSettings: _*)
    .settings(
      name := "fs2 sink http salesforce",
      resolvers += "confluent" at "https://packages.confluent.io/maven/",
      libraryDependencies += scalaTest % Test,
      libraryDependencies ++= http4sLibs,
      libraryDependencies ++= circeLibs,
      libraryDependencies ++= pureConfigLibs
    )
    .dependsOn(sinkCore)
}

// See https://www.scala-sbt.org/1.x/docs/Using-Sonatype.html for instructions on how to publish to Sonatype.
