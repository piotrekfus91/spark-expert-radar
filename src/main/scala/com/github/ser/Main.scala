package com.github.ser

import org.apache.spark.{SparkConf, SparkContext}

import scala.xml.XML

object Main {
  def main(args: Array[String]): Unit = {
    val inputFile = args(0)

    val sparkConf = new SparkConf().setAppName("Test app").setMaster("local[*]")
    val sc = new SparkContext(sparkConf)

    val input = sc.textFile(inputFile)
    input.filter(_.contains("<row"))
      .map(toUser)
      .filter(_.location.isDefined)
      .foreach(println)

    sc.stop()
  }

  def toUser(line: String): User = User(
    attribute(line, "Id").get.toLong,
    attribute(line, "DisplayName").get,
    attribute(line, "Location")
  )

  def attribute(line: String, attributeName: String): Option[String] =
    XML.loadString(line).attribute(attributeName).map(_.text).filter(_ != "")
}

case class User(id: Long, displayName: String, location: Option[String])
