package com.github.ser.domain

import org.scalatest.{FunSuite, Matchers}

class PointTest extends FunSuite with Matchers {
  test("should escape tag") {
    Point(1, "abc", 1).escapedTag shouldBe "abc"
    Point(1, "abc.", 1).escapedTag shouldBe "abc"
    Point(1, ".abc", 1).escapedTag shouldBe "abc"
    Point(1, ".abc.", 1).escapedTag shouldBe "abc"
  }
}
