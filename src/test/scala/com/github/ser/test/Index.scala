package com.github.ser.test

import scala.io.Source

object Index extends Randoms {
  val indexRemovalPrefix = "test"
  val indexPrefix = s"$indexRemovalPrefix-${randomString().toLowerCase()}"
  val userIndex = s"$indexPrefix-user"
  val postIndex = s"$indexPrefix-post"
  val userMapping = Source.fromFile(Index.getClass.getClassLoader.getResource("elasticsearch/user_mapping.json").getPath).mkString
  val postMapping = Source.fromFile(Index.getClass.getClassLoader.getResource("elasticsearch/post_mapping.json").getPath).mkString
}
