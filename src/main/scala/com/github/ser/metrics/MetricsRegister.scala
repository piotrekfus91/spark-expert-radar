package com.github.ser.metrics

import java.util.function.ToDoubleFunction

import com.redis.RedisClient
import com.sksamuel.elastic4s.http.ElasticDsl._
import com.sksamuel.elastic4s.http.HttpClient
import io.micrometer.core.instrument.Metrics
import io.micrometer.core.instrument.binder.jvm.{ClassLoaderMetrics, JvmGcMetrics, JvmMemoryMetrics, JvmThreadMetrics}
import io.micrometer.core.instrument.binder.system.ProcessorMetrics

object MetricsRegister {
  def redis(redis: RedisClient, prefix: String): Unit = {
    redis.set(s"$prefix.cache.hit", 0)
    redis.set(s"$prefix.cache.miss", 0)

    Metrics.gauge("redis.cache.size", redis, new ToDoubleFunction[RedisClient] {
      override def applyAsDouble(redis: RedisClient): Double = redis.keys(s"$prefix*").map(_.size).getOrElse(-1).toDouble
    })

    Metrics.gauge("redis.cache.hit", redis, new ToDoubleFunction[RedisClient] {
      import com.redis.serialization.Parse.Implicits.parseInt
      override def applyAsDouble(value: RedisClient): Double = redis.get[Int](s"$prefix.cache.hit").getOrElse(-1).toDouble
    })

    Metrics.gauge("redis.cache.miss", redis, new ToDoubleFunction[RedisClient] {
      import com.redis.serialization.Parse.Implicits.parseInt
      override def applyAsDouble(value: RedisClient): Double = redis.get[Int](s"$prefix.cache.miss").getOrElse(-1).toDouble
    })
  }

  def elasticsearch(client: HttpClient, indexPrefix: String) = {
    val userIndex = s"$indexPrefix-user"
    val postIndex = s"$indexPrefix-post"

    Metrics.gauge("elasticsearch.user.count", client, new ToDoubleFunction[HttpClient] {
      override def applyAsDouble(value: HttpClient): Double = queryElasticForHits("userId:[* TO *]", client, userIndex)
    })

    Metrics.gauge("elasticsearch.post.count", client, new ToDoubleFunction[HttpClient] {
      override def applyAsDouble(value: HttpClient): Double = queryElasticForHits("postId:[* TO *]", client, postIndex)
    })

    def queryElasticForHits(query: String, client: HttpClient, index: String): Long = {
      val response = client.execute {
        search(index) query query
      }.await

      response match {
        case Left(_) => -1
        case Right(r) =>
          r.result.hits.total
      }
    }
  }


  def jvm() = {
    List(
      new ClassLoaderMetrics(),
      new JvmMemoryMetrics(),
      new JvmGcMetrics(),
      new ProcessorMetrics(),
      new JvmThreadMetrics()
    ).foreach(metrics => metrics.bindTo(Metrics.globalRegistry))
  }
}
