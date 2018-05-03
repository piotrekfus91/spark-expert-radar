package com.github.ser.test

import scala.util.Random

trait Randoms {
  def randomString(length: Int = 3): String = Random.alphanumeric.take(length).mkString
}
