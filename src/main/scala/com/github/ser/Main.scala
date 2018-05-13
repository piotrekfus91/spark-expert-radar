package com.github.ser

import com.github.ser.setup.ElasticsearchSetup
import com.redis.RedisClient
import com.sksamuel.elastic4s.ElasticsearchClientUri
import com.sksamuel.elastic4s.http.HttpClient
import com.typesafe.scalalogging.LazyLogging
import org.apache.spark.{SparkConf, SparkContext}

import scala.io.Source

object Main extends App with LazyLogging {
  val usersPath = args(0)
  logger.info(s"users file: $usersPath")

  val esClient = HttpClient(ElasticsearchClientUri("localhost", 9200))
  val indexName = "users"

  val esSetup = new ElasticsearchSetup(esClient)
  esSetup.setupIndex(indexName, Source.fromFile(Main.getClass.getClassLoader.getResource("elasticsearch/user_mapping.json").getPath).mkString)

  val sparkConf = new SparkConf()
    .setAppName(this.getClass.getSimpleName)
    .setMaster("local[*]")
    .set("es.index", indexName)

  val sc = new SparkContext(sparkConf)

  val redis = new RedisClient("localhost", 6379) with Serializable
  val geoResultCache = new RedisGeoResultCache(redis, "geoResult")

  val reader = new Reader(sc)
  val cleaner = new Cleaner(sc)
  val geocoder = new Geocoder(sc, "https://nominatim.openstreetmap.org", geoResultCache)
  val esSaver = new EsSaver(sc)

  val users = Seq(
    cleaner.cleanUsers _,
    geocoder.fetchGeoResults _,
    esSaver.saveUsersInEs _
  ).reduce(_ andThen _)(reader.loadUsers(usersPath))

  logger.info(s"User indexing finished, indexed: ${users.count()}")

  sc.stop()
}
