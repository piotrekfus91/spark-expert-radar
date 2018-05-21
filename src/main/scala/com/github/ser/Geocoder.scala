package com.github.ser

import com.github.ser.domain.{BoundingBox, GeoResult, User}
import com.typesafe.scalalogging.LazyLogging
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.HttpClients
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD

import scala.io.Source
import scala.util.parsing.json.JSON

class Geocoder(sc: SparkContext, host: String, geoResultCache: GeoResultCache, geoEngine: GeoEngine) extends LazyLogging with Serializable {
  def fetchGeoResults(users: RDD[User]): RDD[User] = {
    logger.info("fetching geo results")
    users.map { user =>
      logger.debug(s"fetching geolocation for $user")
      user.location.map { location =>
        val geoResults = user.location.flatMap(geoResultCache.get).getOrElse {
          def fetchJson: Option[Any] = {
            val httpClient = HttpClients.createDefault()
            val get = new HttpGet(geoEngine.buildQuery(host, location))
            val response = httpClient.execute(get)
            val string = Source.fromInputStream(response.getEntity.getContent).mkString
            val json = JSON.parseFull(string)
            logger.trace(s"geocoder response: $json")
            json
          }

          val json = fetchJson
          val geoResults = geoEngine.parse(user, json)
          geoResultCache.save(location, geoResults)
          logger.debug(s"georesults $geoResults")
          geoResults
        }
        user.copy(geoResults = geoResults.sortBy(_.importance).reverse)
      }.getOrElse(user)
    }
  }
}

trait GeoEngine {
  def buildQuery(host: String, location: String): String
  def parse(user: User, json: Any): List[GeoResult]
}

class NominatimGeoEngine extends GeoEngine with LazyLogging with Serializable {
  override def buildQuery(host: String, location: String): String = s"$host/search?q=${location.replace(" ", "+")}&format=json"

  override def parse(user: User, json: Any): List[GeoResult] = json match {
    case None =>
      logger.warn(s"cannot fetch georesults for $user")
      List.empty
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
}
