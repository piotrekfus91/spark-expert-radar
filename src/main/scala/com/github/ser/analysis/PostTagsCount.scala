package com.github.ser.analysis

object PostTagsCount extends App with AnalysisBase {
  reader.loadPosts("/media/pfus/MAIN/ser/Posts.xml")
    .flatMap(_.tags)
    .map((_, 1))
    .reduceByKey(_ + _)
    .sortBy(_._2)
    .collect()
    .foreach(println)
}
