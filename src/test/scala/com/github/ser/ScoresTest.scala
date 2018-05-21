package com.github.ser

import com.github.ser.domain.Point
import org.scalatest.{FunSuite, Matchers}

class ScoresTest extends FunSuite with Matchers {
  test("should return empty map for empty input") {
    Scores(List.empty).sum shouldBe empty
    Scores(List.empty).avg shouldBe empty
    Scores(List.empty).count shouldBe empty
  }

  test("should count for distinct values") {
    val points = List(
      Point(1, "java", 123),
      Point(2, "kotlin", -321)
    )

    val scores = Scores(points)
    scores.sum shouldBe Map(
      "java" -> 123,
      "kotlin" -> -321
    )

    scores.avg shouldBe Map(
      "java" -> 123,
      "kotlin" -> -321
    )

    scores.count shouldBe Map(
      "java" -> 1,
      "kotlin" -> 1
    )
  }

  test("should count for non distinct values") {
    val points = List(
      Point(1, "java", 123),
      Point(2, "kotlin", -321),
      Point(3, "java", 369),
      Point(4, "kotlin", 321)
    )

    val scores = Scores(points)
    scores.sum shouldBe Map(
      "java" -> 492,
      "kotlin" -> 0
    )

    scores.avg shouldBe Map(
      "java" -> 246,
      "kotlin" -> 0
    )

    scores.count shouldBe Map(
      "java" -> 2,
      "kotlin" -> 2
    )
  }
}
