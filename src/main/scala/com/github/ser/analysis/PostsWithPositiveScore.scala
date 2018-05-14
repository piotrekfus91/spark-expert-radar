package com.github.ser.analysis

object PostsWithPositiveScore extends App with AnalysisBase {
  val count = reader.loadPosts("/media/pfus/MAIN/ser/Posts_50000.xml")
    .filter(_.score > 0)
    .count()

  println(s"Positive scores count: $count")
}
