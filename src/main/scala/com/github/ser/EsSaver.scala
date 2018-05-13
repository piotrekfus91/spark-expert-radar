package com.github.ser

import com.github.ser.domain.{Post, User}
import com.typesafe.scalalogging.LazyLogging
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD
import org.elasticsearch.spark._

class EsSaver(sc: SparkContext) extends LazyLogging {
  def saveUsersInEs(users: RDD[User]): RDD[User] = {
    logger.info("saving users to ES")
    users.map { user =>
      Map(
        "userId" -> user.id,
        "displayName" -> user.displayName,
        "location" -> user.location.getOrElse(""),
        "geolocation" -> user.geoResults.find(_ => true).map(geoResult => Seq(geoResult.latitude, geoResult.longitude)).map(_.mkString(",")).getOrElse("")
      )
    }.saveToEs(s"${sc.getConf.get("es.index")}-user/doc", Map("es.mapping.id" -> "userId"))
    users
  }

  def savePostsInEs(posts: RDD[Post]): RDD[Post] = {
    logger.info("saving posts to ES")
    posts.map { post =>
      Map(
        "postId" -> post.id,
        "parentId" -> post.parentId.orElse(null),
        "postType" -> post.postType.name,
        "score" -> post.score,
        "tags" -> post.tags
      )
    }.saveToEs(s"${sc.getConf.get("es.index")}-post/doc", Map("es.mapping.id" -> "postId"))
    posts
  }
}
