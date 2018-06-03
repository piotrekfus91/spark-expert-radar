package com.github.ser

import com.github.ser.domain.{Post, PostType, User}
import com.github.ser.metrics.Metered
import com.github.ser.util.XmlUtil
import org.apache.spark.sql.{Dataset, Encoder}

class Reader extends SparkProvider {
  def loadUsers(inputFile: String)(implicit userEncoder: Encoder[User]): Dataset[User] = {
    Metered.timed("component.reader", "object", "user", "measurement", "total")(() => loadFile(inputFile, "user").map(toUserCustomParser))
  }

  def loadPosts(inputFile: String)(implicit postEncoder: Encoder[Post]): Dataset[Post] = {
    Metered.timed("component.reader", "object", "post", "measurement", "total")(() => loadFile(inputFile, "post").map(toPostCustomParser))
  }

  private def loadFile(inputFile: String, `object`: String): Dataset[String] = {
    import spark.implicits._
    Metered.timed("component.reader", "object", `object`, "measurement", "fileLoading")(() => {
      sqlContext.read.textFile(inputFile).as[String]
        .filter(_.contains("<row"))
    })
  }

  val toUserXmlUtil = (line: String) => Metered.timed("parsing", "object", "user", "parser", "xml")(() => User(
    XmlUtil.requiredAttribute(line, "Id").toLong,
    XmlUtil.requiredAttribute(line, "DisplayName"),
    XmlUtil.optionalAttribute(line, "Location"),
    XmlUtil.requiredAttribute(line, "UpVotes").toLong,
    XmlUtil.requiredAttribute(line, "DownVotes").toLong
  ))

  val toUserCustomParser = (line: String) => Metered.timed("parsing", "object", "user", "parser", "regex")(() => {
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
  })

  val toPostCustomParser = (line: String) => Metered.timed("parsing", "object", "post", "parser", "regex")(() => {
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
  })
}
