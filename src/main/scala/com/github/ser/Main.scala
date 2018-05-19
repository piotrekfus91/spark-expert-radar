package com.github.ser

import com.github.ser.domain.Answer
import com.github.ser.elasticsearch.{ElasticsearchSetup, Query}
import com.redis.RedisClient
import com.sksamuel.elastic4s.ElasticsearchClientUri
import com.sksamuel.elastic4s.http.HttpClient
import com.typesafe.scalalogging.LazyLogging
import org.apache.spark.{SparkConf, SparkContext}

import scala.io.Source

object Main extends App with LazyLogging {
  val usersPath = args(0)
  logger.info(s"users file: $usersPath")
  val postsPath = args(1)
  logger.info(s"posts file: $postsPath")

  val indexName = "ser"

  val esClient = HttpClient(ElasticsearchClientUri("localhost", 9200))
  setupElastic(esClient)
  val query = new Query(esClient, indexName)

  val sc = setupSpark

  val redis = new RedisClient("localhost", 6379) with Serializable
  val geoResultCache = new RedisGeoResultCache(redis, "geoResult")

  val reader = new Reader(sc)
  val cleaner = new Cleaner(sc)
  val geocoder = new Geocoder(sc, "https://nominatim.openstreetmap.org", geoResultCache)
  val esSaver = new EsSaver(sc)

//  val users = Seq(
//    cleaner.cleanUsers _,
//    esSaver.saveUsersInEs _
//  ).reduce(_ andThen _)(reader.loadUsers(usersPath))

//  logger.info(s"User indexing finished, indexed: ${users.count()}")

  val posts = Seq(
    cleaner.cleanPosts _,
    esSaver.savePostsInEs _
  ).reduce(_ andThen _)(reader.loadPosts(postsPath))

  val p = reader.loadPosts(postsPath)
    .filter(_.postType == Answer)
    .map { post =>
      val maybeUserWithTags = for {
        parentId <- post.parentId
        questionPost <- query.queryPostsSingle(s"postId:$parentId")
        userId <- post.ownerUserId
        user <- query.queryUsersSingle(s"userId:$userId")
      } yield (user, questionPost)
      maybeUserWithTags.foreach(println)
      post
    }.collect()

  sc.stop()

  private def setupElastic(esClient: HttpClient) = {
    val esSetup = new ElasticsearchSetup(esClient)
    esSetup.setupIndex(s"$indexName-user", Source.fromFile(Main.getClass.getClassLoader.getResource("elasticsearch/user_mapping.json").getPath).mkString)
    esSetup.setupIndex(s"$indexName-post", Source.fromFile(Main.getClass.getClassLoader.getResource("elasticsearch/post_mapping.json").getPath).mkString)
  }

  private def setupSpark = {
    val sparkConf = new SparkConf()
      .setAppName(this.getClass.getSimpleName)
      .setMaster("local[*]")
      .set("es.index", indexName)

    new SparkContext(sparkConf)
  }
}
