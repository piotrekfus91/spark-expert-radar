name := "spark-expert-radar"

version := "0.1"

scalaVersion := "2.11.12"

val spark = new {
  val version = "2.3.0"
  val modules = Seq("spark-core")
  val deps = modules.map { "org.apache.spark" %% _ % version }
}

val test = new {
  val deps = Seq(
    "org.scalatest" %% "scalatest" % "3.0.1" % "test",
    "org.scalamock" %% "scalamock-scalatest-support" % "3.4.2" % "test",
    "org.scalacheck" %% "scalacheck" % "1.13.4" % "test"
  )
}

val dependencies = Seq(spark, test)

libraryDependencies ++= dependencies.flatMap(_.deps)
