package com.github.ser.analysis

object UniqueLocation extends AnalysisBase with App {
  import spark.implicits._
  val uniqueLocations = reader.loadUsers("/home/pfus/Studia/bigdata/projekt/data/Users.xml")
    .flatMap(_.location)
    .map(_.toLowerCase())
    .distinct()
    .count()

  println(s"Unique locations: $uniqueLocations")
}
