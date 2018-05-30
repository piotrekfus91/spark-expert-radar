package com.github.ser

import com.github.ser.domain._
import com.github.ser.test.Spark
import org.scalatest.{Matchers, WordSpec}

class CleanerTest extends WordSpec with Matchers with Spark {

  val technicalUser = User(-1, "technical", Some("Somewhere"))
  val normalUser = User(1, "normal user", Some("Warsaw"))
  val homelessUser = User(1, "homeless user", None)
  val userList = List(technicalUser, normalUser, homelessUser)

  val normalPost = Post(1, None, Question, 1, Some(1), List("tag"))
  val unknownPostType = Post(2, None, Other(1), 2, Some(2), List("tag"))
  val emptyTagsPost = Post(3, None, Answer, 3, Some(3), List.empty)
  val postList = List(normalPost, unknownPostType)

  import spark.implicits._

  val sut = new Cleaner()

  "Cleaner" when {
    "clean users" should {
      val cleanedUsers = sut.cleanUsers(sc.parallelize(userList).toDS).collect()

      "contain normal user" in {
        cleanedUsers should contain(normalUser)
      }

      "not contain technical user" in {
        cleanedUsers should not contain(technicalUser)
      }

      "not contain homeless user" in {
        cleanedUsers should not contain(homelessUser)
      }
    }

    "clean posts" should {
      val cleanedPosts = sut.cleanPosts(sc.parallelize(postList).toDS).collect()

      "contain normal post" in {
        cleanedPosts should contain(normalPost)
      }

      "not contain unknown post type" in {
        cleanedPosts should not contain(unknownPostType)
      }

      "not contain post without tags" in {
        cleanedPosts should not contain(emptyTagsPost)
      }
    }
  }
}
