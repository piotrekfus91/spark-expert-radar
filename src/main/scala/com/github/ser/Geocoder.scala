package com.github.ser

import com.github.ser.domain.{BoundingBox, GeoResult, User}
import com.typesafe.scalalogging.LazyLogging
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.HttpClients
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD

import scala.io.Source
import scala.util.parsing.json.JSON

class Geocoder(sc: SparkContext, host: String, geoResultCache: GeoResultCache) extends LazyLogging with Serializable {
  def fetchGeoResults(users: RDD[User]): RDD[User] = {
    users.map { user =>
      user.location.map { location =>
        val geoResults = user.location.flatMap(geoResultCache.get).getOrElse {
          def fetchJson: Option[Any] = {
            val httpClient = HttpClients.createDefault()
            val get = new HttpGet(s"$host/search?q=${location.replace(" ", "+")}&format=json")
            val response = httpClient.execute(get)
            val string = Source.fromInputStream(response.getEntity.getContent).mkString
            val json = JSON.parseFull(string)
            json
          }

          val json = fetchJson
          val geoResults = json match {
            case None => List.empty
            case Some(parsed) =>
              parsed.asInstanceOf[List[Map[String, Any]]].map { entry =>
                val boundingBox = entry("boundingbox").asInstanceOf[List[String]].map(_.toDouble)
                GeoResult(
                  displayName = entry("display_name").asInstanceOf[String],
                  latitude = entry("lat").asInstanceOf[String].toDouble,
                  longitude = entry("lon").asInstanceOf[String].toDouble,
                  importance = entry("importance").asInstanceOf[Double],
                  boundingBox = BoundingBox(
                    westLatitude = boundingBox(0),
                    eastLatitude = boundingBox(1),
                    northLongitude = boundingBox(2),
                    southLongitude = boundingBox(3)
                  )
                )
              }
          }
          geoResultCache.save(user.location.get, geoResults)
          geoResults
        }
        user.copy(geoResults = geoResults.sortBy(_.importance).reverse)
      }.getOrElse(user)
    }
  }
}
