package com.github.ser.analysis

object PostsCount extends AnalysisBase with App {
  val count = reader.loadPosts("/media/pfus/MAIN/ser/Posts.xml").count()
  println(s"Posts count: $count")
}
