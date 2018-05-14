package com.github.ser.analysis

object PostsGroupedByPostTypeCount extends App with AnalysisBase {
  val postTypes = reader.loadPosts("/media/pfus/MAIN/ser/Posts_50000.xml")
    .map(post => (post.postType, 1))
    .reduceByKey(_ + _)
    .collect()
  postTypes.foreach(postType => println(s"Post type: ${postType._1}, count: ${postType._2}"))
}
