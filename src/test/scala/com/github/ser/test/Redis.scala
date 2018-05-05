package com.github.ser.test

import com.redis.RedisClient

object Redis {
  val client = new RedisClient("localhost", 6379) with Serializable
}
