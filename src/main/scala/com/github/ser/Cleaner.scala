package com.github.ser

import com.github.ser.domain.{Answer, Post, Question, User}
import com.github.ser.metrics.Metered
import com.typesafe.scalalogging.LazyLogging
import org.apache.spark.sql.Dataset

class Cleaner extends LazyLogging {
  def cleanUsers(users: Dataset[User]): Dataset[User] = {
    logger.info("cleaning users")
    Metered.timed("component.cleaner", "object", "user")(() => {
      List(
        removeSpecialUsers,
        removeWithoutLocation
      ).reduce(_ andThen _)(users)
    })
  }

  private val removeSpecialUsers = (users: Dataset[User]) => users.filter(_.id > 0)
  private val removeWithoutLocation = (users: Dataset[User]) => users.filter(_.location.isDefined)

  def cleanPosts(posts: Dataset[Post]): Dataset[Post] = {
    logger.info("cleaning posts")
    Metered.timed("component.cleaner", "object", "post")(() => {
      List(
        removePostsWithUnknownPostType
      ).reduce(_ andThen _)(posts)
    })
  }

  private val removePostsWithUnknownPostType = (posts: Dataset[Post]) => posts.filter(post => List(Question, Answer).contains(post.postType))
}
