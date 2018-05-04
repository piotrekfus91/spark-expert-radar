package com.github.ser.analysis

import com.github.ser.Reader
import org.apache.spark.{SparkConf, SparkContext}

object UniqueLocation extends App {
  val sparkConf = new SparkConf()
    .setAppName(this.getClass.getSimpleName)
    .setMaster("local[*]")

  val sc = new SparkContext(sparkConf)

  val reader = new Reader(sc)

  val uniqueLocations = reader.loadUsers("/home/pfus/Studia/bigdata/projekt/data/Users.xml")
    .filter(_.location.isDefined)
    .map(_.location.get)
    .map(_.toLowerCase())
    .distinct()
    .count()

  println(s"Unique locations: $uniqueLocations")
}
