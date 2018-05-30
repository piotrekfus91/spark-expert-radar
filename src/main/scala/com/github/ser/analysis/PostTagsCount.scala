package com.github.ser.analysis

object PostTagsCount extends App with AnalysisBase {
  import spark.implicits._

  reader.loadPosts("/media/pfus/MAIN/ser/Posts.xml")
    .flatMap(_.tags)
    .groupBy($"value")
    .count()
    .show()
}
