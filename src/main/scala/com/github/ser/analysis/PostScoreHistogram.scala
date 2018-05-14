package com.github.ser.analysis

object PostScoreHistogram extends App with AnalysisBase {
  val scoreHistogram = reader.loadPosts("/media/pfus/MAIN/ser/Posts_50000.xml")
    .map(_.score)
    .histogram(5)

  scoreHistogram._1.zip(scoreHistogram._2).foreach(bucket => println(s"${bucket._1}: ${bucket._2}"))
}
