package com.github.ser.analysis

object UsersCount extends AnalysisBase with App {
  val count = reader.loadUsers("/home/pfus/Studia/bigdata/projekt/data/Users.xml").count()
  println(s"Users count: $count")
}
