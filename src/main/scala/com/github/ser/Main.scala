package com.github.ser

import com.github.ser.domain.{Answer, Point, Post, User}
import com.github.ser.elasticsearch.ElasticsearchSetup
import com.github.ser.metrics.{MetricsRegister, Micrometer}
import com.sksamuel.elastic4s.http.HttpClient
import com.typesafe.scalalogging.LazyLogging
import io.micrometer.core.instrument.Metrics

import scala.io.Source

object Main extends App with SparkProvider with Resources with LazyLogging {

  val usersPath = args(0)
  logger.info(s"users file: $usersPath")
  val postsPath = args(1)
  logger.info(s"posts file: $postsPath")

  override def geoEngineMode = NominatimMode

  setupElastic(esClient)
  setupMetrics

//  val users = Seq(
//    cleaner.cleanUsers _,
//    esSaver.saveUsersInEs _
//  ).reduce(_ andThen _)(reader.loadUsers(usersPath))

  val posts = Seq(
    cleaner.cleanPosts _,
    esSaver.savePostsInEs _
  ).reduce(_ andThen _)(reader.loadPosts(postsPath))

  val usersToUpdate = reader.loadPosts(postsPath)
    .filter(_.postType == Answer)
    .flatMap { post =>
      val maybeUserWithTags = for {
        parentId <- post.parentId
        questionPost <- query.queryPostsSingle(s"postId:$parentId")
        userId <- post.ownerUserId
        user <- query.queryUsersSingle(s"userId:$userId")
      } yield (user, questionPost)
      maybeUserWithTags.map { case (user: User, post: Post) =>
        val otherPostPoints = user.points.filter(_.postId != post.id)
        val addedTags = post.tags.map(tag => Point(post.id, tag, post.score))
        user.copy(points = otherPostPoints ++ addedTags)
      }
    }

  Seq(
    geocoder.fetchGeoResults _,
    esSaver.saveUsersInEs _
  ).reduce(_ andThen _)(usersToUpdate)

  println("finished")

  spark.stop()

  private def setupElastic(esClient: HttpClient) = {
    val esSetup = new ElasticsearchSetup(esClient)
    esSetup.setupIndex(s"$indexName-user", Source.fromFile(Main.getClass.getClassLoader.getResource("elasticsearch/user_mapping.json").getPath).mkString)
    esSetup.setupIndex(s"$indexName-post", Source.fromFile(Main.getClass.getClassLoader.getResource("elasticsearch/post_mapping.json").getPath).mkString)
    spark.conf.set("es.index", indexName)
  }

  private def setupMetrics = {
    Metrics.addRegistry(Micrometer.meterRegistry)
    MetricsRegister.redis(redis, redisPrefix)
    MetricsRegister.elasticsearch(esClient, indexName)
    MetricsRegister.jvm()
  }
}
