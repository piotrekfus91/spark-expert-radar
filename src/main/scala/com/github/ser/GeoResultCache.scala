package com.github.ser

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}

import com.github.ser.domain.GeoResult
import com.redis.RedisClient
import com.redis.serialization.Parse.Implicits.parseByteArray
import com.sksamuel.avro4s.{AvroInputStream, AvroOutputStream}
import com.typesafe.scalalogging.LazyLogging
import org.apache.commons.lang3.StringUtils

import scala.collection.mutable

trait GeoResultCache {
  def save(location: String, geoResults: List[GeoResult]): Unit
  def get(location: String): Option[List[GeoResult]]
}

class EmptyGeoResultCache extends GeoResultCache with Serializable {
  override def save(location: String, geoResults: List[GeoResult]): Unit = {}
  override def get(location: String): Option[List[GeoResult]] = None
}

class MapBasedGeoResultCache extends GeoResultCache with Serializable {
  val cache = mutable.Map[String, List[GeoResult]]()
  override def save(location: String, geoResults: List[GeoResult]): Unit = cache.put(location, geoResults)
  override def get(location: String): Option[List[GeoResult]] = cache.get(location)
}

class RedisGeoResultCache(val redis: RedisClient, val prefix: String) extends GeoResultCache with LazyLogging with Serializable {
  override def save(location: String, geoResults: List[GeoResult]): Unit = {
    logger.trace(s"saving location $location as $geoResults")
    val key = buildKey(location)
    val baos = new ByteArrayOutputStream()
    val out = AvroOutputStream.json[GeoResult](baos)
    out.write(geoResults)
    out.close()
    redis.set(key, baos)
  }

  override def get(location: String): Option[List[GeoResult]] = {
    val key = buildKey(location)
    val maybeBytes = redis.get[Array[Byte]](key)
    logger.trace(s"got geolocation from cache for $location: $maybeBytes")
    val entry = maybeBytes.map { bytes =>
      val bais = new ByteArrayInputStream(bytes)
      val in = AvroInputStream.json[GeoResult](bais)
      in.iterator.toList
    }
    entry match {
      case Some(_) => redis.incr(s"$prefix.cache.hit")
      case None => redis.incr(s"$prefix.cache.miss")
    }
    entry
  }

  private def buildKey(location: String) = {
    val suffix = StringUtils.stripAccents(location.toLowerCase().toCharArray.filter(_.isLetterOrDigit).mkString)
    s"$prefix:$suffix"
  }
}
