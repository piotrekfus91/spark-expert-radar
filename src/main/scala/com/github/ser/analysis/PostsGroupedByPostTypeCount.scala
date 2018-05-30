package com.github.ser.analysis

object PostsGroupedByPostTypeCount extends App with AnalysisBase {
  import spark.implicits._
  reader.loadPosts("data/Posts_1000.xml")
    .map(_.postType.name)
    .groupBy($"value")
    .count()
    .show()
}
