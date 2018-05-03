package com.github.ser.test

import scala.io.Source

object Index extends Randoms {
  val indexPrefix = "test"
  val indexName = s"$indexPrefix-${randomString().toLowerCase()}"
  val rawMapping = Source.fromFile(Index.getClass.getClassLoader.getResource("elasticsearch/index.json").getPath).mkString
}
