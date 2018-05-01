name := "spark-expert-radar"

version := "0.1"

scalaVersion := "2.11.12"

val spark = new {
  val version = "2.3.0"
  val modules = Seq("spark-core")
  val deps = modules.map { "org.apache.spark" %% _ % version }
}

val logging = new {
  val deps = Seq(
    "com.typesafe.scala-logging" %% "scala-logging" % "3.9.0"
  )
}

val util = new {
  val deps = Seq(
    "com.google.guava" % "guava" % "16.0.1"
  )
}

val jackson = new {
  val version = "2.8.11"
  val deps = Seq(
    "com.fasterxml.jackson.core" % "jackson-core" % version,
    "com.fasterxml.jackson.core" % "jackson-databind" % version,
    "com.fasterxml.jackson.module" %% "jackson-module-scala" % version
  )
}

val test = new {
  val deps = Seq(
    "org.scalatest" %% "scalatest" % "3.0.1" % "test",
    "org.scalamock" %% "scalamock-scalatest-support" % "3.4.2" % "test",
    "org.scalacheck" %% "scalacheck" % "1.13.4" % "test",
    "com.github.tomakehurst" % "wiremock" % "2.17.0" % "test"
  )
}

val dependencies = Seq(spark, logging, util, test)

libraryDependencies ++= dependencies.flatMap(_.deps)

dependencyOverrides ++= jackson.deps
