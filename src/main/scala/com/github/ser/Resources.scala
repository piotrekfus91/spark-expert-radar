package com.github.ser

import com.github.ser.elasticsearch.Query
import com.redis.RedisClient
import com.sksamuel.elastic4s.ElasticsearchClientUri
import com.sksamuel.elastic4s.http.HttpClient

trait Resources extends ElasticResources with CacheResources with SparkResources {
  def geoEngineMode: GeoEngineMode

  val apiKey = System.getProperty("google.api.key")
}

trait ElasticResources {
  val indexName = "ser"
  val esClient = HttpClient(ElasticsearchClientUri("localhost", 9200))
  val query = new Query(esClient, indexName)
}

trait CacheResources {
  this: Resources =>

  val redisPrefix = geoEngineMode.geoEngineName.toLowerCase()
  val redis = new RedisClient("localhost", 6379) with Serializable
  val geoResultCache = new RedisGeoResultCache(redis, geoEngineMode.geoEngineName.toLowerCase())
}

trait SparkResources {
  this: Resources =>

  val reader = new Reader()
  val cleaner = new Cleaner()
  val esSaver = new EsSaver()
  val geocoder = geoEngineMode match {
    case NominatimMode => new Geocoder("https://nominatim.openstreetmap.org", geoResultCache, new TimedGeoEngine(new NominatimGeoEngine))
    case GoogleMode => new Geocoder("https://maps.googleapis.com", geoResultCache, new TimedGeoEngine(new GoogleGeoEngine(apiKey)))
  }
  val tagCounter = new TagCounter()
}

sealed trait GeoEngineMode {
  val geoEngineName: String
}

case object NominatimMode extends GeoEngineMode {
  override val geoEngineName: String = "Nominatim"
}

case object GoogleMode extends GeoEngineMode {
  override val geoEngineName: String = "Google"
}
