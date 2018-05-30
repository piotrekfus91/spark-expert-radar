package com.github.ser

import com.github.ser.domain.{Post, PostType, User}
import com.github.ser.util.XmlUtil
import org.apache.spark.sql.{Dataset, Encoder}

class Reader extends SparkProvider {
  def loadUsers(inputFile: String)(implicit userEncoder: Encoder[User]): Dataset[User] = {
    loadFile(inputFile).map(toUserCustomParser)
  }

  def loadPosts(inputFile: String)(implicit postEncoder: Encoder[Post]): Dataset[Post] = {
    loadFile(inputFile).map(toPostCustomParser)
  }

  private def loadFile(inputFile: String): Dataset[String] = {
    import spark.implicits._
    sqlContext.read.textFile(inputFile).as[String]
      .filter(_.contains("<row"))
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
      extractAttribute(attributeName).getOrElse(throw new RuntimeException(s"cannot find attribute $attributeName in $line"))
    }

    User(
      extractRequiredAttribute("Id").toLong,
      extractRequiredAttribute("DisplayName"),
      extractAttribute("Location"),
      extractRequiredAttribute("Reputation").toLong,
      extractRequiredAttribute("UpVotes").toLong,
      extractRequiredAttribute("DownVotes").toLong
    )
  }

  val toPostCustomParser = (line: String) => {
    val allQuotes = "\"".r.findAllMatchIn(line).map(_.start).sliding(2, 2).toList
    val attributeNamesStart = (line.indexOf("<row ") + "<row ".length) :: allQuotes.map(l => l(1) + 2).dropRight(1)
    val attributeNames = attributeNamesStart.zip(allQuotes.map(_.head - 1)).map(p => line.substring(p._1, p._2))
    val attributes = attributeNames.zip(allQuotes).toMap

    def extractAttribute(attributeName: String): Option[String] = {
      attributes.get(attributeName).map(x => line.substring(x(0) + 1, x(1)))
    }

    def extractRequiredAttribute(attributeName: String): String = {
      extractAttribute(attributeName).getOrElse(throw new RuntimeException(s"cannot find attribute $attributeName in $line"))
    }

    Post(
      extractRequiredAttribute("Id").toLong,
      extractAttribute("ParentId").map(_.toLong),
      PostType(extractRequiredAttribute("PostTypeId")),
      extractRequiredAttribute("Score").toLong,
      extractAttribute("OwnerUserId").map(_.toLong),
      extractAttribute("Tags").map(_.split("&gt;").map(_.substring("&lt;".length)).toList).getOrElse(List.empty)
    )
  }
}
