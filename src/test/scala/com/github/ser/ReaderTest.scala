package com.github.ser

import com.github.ser.domain.{Answer, Post, Question, User}
import org.apache.spark.SparkContext
import org.scalatest.{FlatSpec, Matchers}

class ReaderTest(sc: SparkContext) extends FlatSpec with Matchers {
  val sut = new Reader(sc)

  it should "read users file to RDD" in {
    val users = sut.loadUsers(this.getClass.getClassLoader.getResource("Users_2.xml").getPath).collect()

    users should contain allOf(
      User(1, "Jeff Atwood", Some("El Cerrito, CA"), 43078, 3352, 1308),
      User(2, "Geoff Dalgas", None, 3345, 646, 88)
    )
  }

  it should "read posts file to RDD" in {
    val posts = sut.loadPosts(this.getClass.getClassLoader.getResource("Posts_2.xml").getPath).collect()

    posts should contain allOf(
      Post(4, None, Question, 543, Some(8), List("c#", "winforms", "type-conversion", "decimal", "opacity")),
      Post(7, Some(4), Answer, 391, Some(9), List.empty)
    )
  }
}
