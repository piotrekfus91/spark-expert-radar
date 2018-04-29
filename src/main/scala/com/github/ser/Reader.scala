package com.github.ser

import com.github.ser.domain.User
import com.github.ser.util.XmlUtil
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD

class Reader(sc: SparkContext) {
  def loadUsers(inputFile: String): RDD[User] = {
    sc.textFile(inputFile)
      .filter(_.contains("<row"))
      .map(toUser)
  }

  val toUser = (line: String) => User(
    XmlUtil.requiredAttribute(line, "Id").toLong,
    XmlUtil.requiredAttribute(line, "DisplayName"),
    XmlUtil.optionalAttribute(line, "Location")
  )
}
