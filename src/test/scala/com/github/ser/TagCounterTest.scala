package com.github.ser

import com.github.ser.domain.{Answer, Post}
import com.github.ser.test.Spark
import org.scalatest.{FlatSpec, Matchers}

class TagCounterTest extends FlatSpec with Matchers with Spark {
  import spark.implicits._

  val sut = new TagCounter()

  it should "count tags" in {
    val posts = List(
      Post(1, None, Answer, 1, None, List("java", "scala")),
      Post(1, None, Answer, 1, None, List("java")),
      Post(1, None, Answer, 1, None, List("kotlin"))
    ).toDS
    sut.countTags(posts, 10) shouldBe Map(
      "java" -> 2,
      "scala" -> 1,
      "kotlin" -> 1
    )
  }

  it should "count tags with limit" in {
    val posts = List(
      Post(1, None, Answer, 1, None, List("java", "scala")),
      Post(1, None, Answer, 1, None, List("java")),
      Post(1, None, Answer, 1, None, List("kotlin"))
    ).toDS
    sut.countTags(posts, 1) shouldBe Map(
      "java" -> 2
    )
  }
}
