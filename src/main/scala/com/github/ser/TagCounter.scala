package com.github.ser

import com.github.ser.domain.Post
import org.apache.spark.sql.Dataset

class TagCounter extends SparkProvider {
  import spark.implicits._

  def countTags(posts: Dataset[Post], tagLimit: Int): Map[String, Long] = {
    posts.flatMap(_.tags)
      .rdd
      .map((_, 1L))
      .reduceByKey(_ + _)
      .sortBy(_._2, false)
      .take(tagLimit)
      .toMap
  }
}
