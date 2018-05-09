package com.github.ser

import com.github.ser.domain.User
import com.github.ser.util.XmlUtil
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD

class Reader(sc: SparkContext) {
  def loadUsers(inputFile: String): RDD[User] = {
    sc.textFile(inputFile)
      .filter(_.contains("<row"))
      .map(toUserCustomParser)
  }

  val toUserXmlUtil = (line: String) => User(
    XmlUtil.requiredAttribute(line, "Id").toLong,
    XmlUtil.requiredAttribute(line, "DisplayName"),
    XmlUtil.optionalAttribute(line, "Location"),
    XmlUtil.requiredAttribute(line, "UpVotes").toLong,
    XmlUtil.requiredAttribute(line, "DownVotes").toLong
  )

  val toUserCustomParser = (line: String) => {
    val allQuotes = "\"".r.findAllMatchIn(line).map(_.start).sliding(2, 2).toList
    val attributeNamesStart = (line.indexOf("<row ") + "<row ".length) :: allQuotes.map(l => l(1) + 2).dropRight(1)
    val attributeNames = attributeNamesStart.zip(allQuotes.map(_.head - 1)).map(p => line.substring(p._1, p._2))
    val attributes = attributeNames.zip(allQuotes).toMap

    def extractAttribute(attributeName: String): Option[String] = {
      attributes.get(attributeName).map(x => line.substring(x(0) + 1, x(1)))
    }

    def extractRequiredAttribute(attributeName: String): String = {
      extractAttribute(attributeName).getOrElse(throw new RuntimeException(s"cannot find attribute Id in $line"))
    }

    User(
      extractRequiredAttribute("Id").toLong,
      extractRequiredAttribute("DisplayName"),
      extractAttribute("Location"),
      extractRequiredAttribute("UpVotes").toLong,
      extractRequiredAttribute("DownVotes").toLong
    )
  }
}
