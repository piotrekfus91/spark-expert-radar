package com.github.ser

import com.github.ser.domain.User
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD

import scala.xml.XML

class Reader(sc: SparkContext) {
  def loadUsers(inputFile: String): RDD[User] = {
    sc.textFile(inputFile)
      .filter(_.contains("<row"))
      .map(toUser)
  }

  private val toUser = (line: String) => User(
    XML.loadString(line)
      .attribute("Id")
      .map(_.text)
      .getOrElse(throw new RuntimeException(s"cannot find attribute Id in $line")).toLong,
    XML.loadString(line)
      .attribute("DisplayName")
      .map(_.text)
      .getOrElse(throw new RuntimeException(s"cannot find attribute DisplayName in $line")),
    XML.loadString(line)
      .attribute("Location")
      .map(_.text)
  )
}
