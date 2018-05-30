name := "spark-expert-radar"

version := "0.1"

scalaVersion := "2.11.12"

javacOptions ++= Seq("-source", "1.8", "-target", "1.8")

scalacOptions := Seq("-target:jvm-1.8")

val spark = new {
  val version = "2.3.0"
  val modules = Seq("spark-core", "spark-sql")
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

val elasticsearch = new {
  val deps = Seq(
    "org.elasticsearch" %% "elasticsearch-spark-20" % "6.2.4"
  )
}

val elastic4s = new {
  val version = "6.2.6"
  val modules = Seq("elastic4s-core", "elastic4s-http")
  val deps = modules.map { "com.sksamuel.elastic4s" %% _ % version }
}

val redis = new {
  val deps = Seq(
    "net.debasishg" %% "redisclient" % "3.6"
  )
}

val avro = new {
  val deps = Seq(
    "com.sksamuel.avro4s" %% "avro4s-core" % "1.8.3"
  )
}

val metrics = new {
  val deps = Seq(
    "io.micrometer" % "micrometer-registry-influx" % "1.0.4"
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

val dependencies = Seq(spark, logging, util, elasticsearch, elastic4s, redis, avro, metrics, test)

libraryDependencies ++= dependencies.flatMap(_.deps)

dependencyOverrides ++= jackson.deps
