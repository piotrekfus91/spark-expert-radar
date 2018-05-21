package com.github.ser

import com.github.ser.domain.{Point, Post, User}
import com.typesafe.scalalogging.LazyLogging
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD
import org.elasticsearch.spark._

class EsSaver(sc: SparkContext) extends LazyLogging {
  def saveUsersInEs(users: RDD[User]): RDD[User] = {
    logger.info("saving users to ES")
    users.map { user =>
      val scores = Scores(user.points)
      Map(
        "userId" -> user.id,
        "displayName" -> user.displayName,
        "reputation" -> user.reputation,
        "upvotes" -> user.upvotes,
        "downvotes" -> user.downvotes,
        "location" -> user.location.getOrElse(""),
        "geolocation" -> user.geoResults.find(_ => true).map(geoResult => Seq(geoResult.latitude, geoResult.longitude)).map(_.mkString(",")).getOrElse(""),
        "points" -> user.points,
        "scoresSum" -> scores.sum,
        "scoresAvg" -> scores.avg,
        "scoresCount" -> scores.count
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
        "ownerUserId" -> post.ownerUserId,
        "tags" -> post.tags
      )
    }.saveToEs(s"${sc.getConf.get("es.index")}-post/doc", Map("es.mapping.id" -> "postId"))
    posts
  }
}

case class Scores(points: List[Point]) {
  lazy val sum: Map[String, Long] =
    points
      .map(point => (point.escapedTag, point.score))
      .groupBy(_._1)
      .mapValues(_.map(_._2))
      .mapValues(_.sum)

  lazy val count: Map[String, Long] =
    points
      .map(point => (point.escapedTag, 1))
      .groupBy(_._1)
      .mapValues(_.size)

  lazy val avg: Map[String, Double] = sum.map { case (tag, sum) => (tag, sum.toDouble / points.count(_.escapedTag == tag)) }
}
