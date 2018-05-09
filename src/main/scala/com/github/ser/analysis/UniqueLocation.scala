package com.github.ser.analysis

object UniqueLocation extends AnalysisBase with App {
  val uniqueLocations = reader.loadUsers("/home/pfus/Studia/bigdata/projekt/data/Users.xml")
    .filter(_.location.isDefined)
    .map(_.location.get)
    .map(_.toLowerCase())
    .distinct()
    .count()

  println(s"Unique locations: $uniqueLocations")
}
