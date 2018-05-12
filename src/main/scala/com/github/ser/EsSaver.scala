package com.github.ser

import com.github.ser.domain.User
import com.typesafe.scalalogging.LazyLogging
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD
import org.elasticsearch.spark._

class EsSaver(sc: SparkContext) extends LazyLogging {
  def saveUsersInEs(users: RDD[User]): RDD[User] = {
    logger.info("saving users to ES")
    users.map { user =>
      Map(
        "userId" -> user.id,
        "displayName" -> user.displayName,
        "location" -> user.location.getOrElse(""),
        "geolocation" -> user.geoResults.find(_ => true).map(geoResult => Seq(geoResult.latitude, geoResult.longitude)).map(_.mkString(",")).getOrElse("")
      )
    }.saveToEs(s"${sc.getConf.get("es.index")}/doc", Map("es.mapping.id" -> "userId"))
    users
  }
}
