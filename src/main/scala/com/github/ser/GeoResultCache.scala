package com.github.ser

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}

import com.github.ser.domain.GeoResult
import com.redis.RedisClient
import com.redis.serialization.Parse.Implicits.parseByteArray
import com.sksamuel.avro4s.{AvroInputStream, AvroOutputStream}
import org.apache.commons.lang3.StringUtils

trait GeoResultCache {
  def save(location: String, georesults: List[GeoResult]): Unit
  def get(location: String): Option[List[GeoResult]]
}

class RedisGeoResultCache(val redis: RedisClient, val prefix: String) extends GeoResultCache {

  override def save(location: String, geoResults: List[GeoResult]): Unit = {
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
    maybeBytes.map { bytes =>
      val bais = new ByteArrayInputStream(bytes)
      val in = AvroInputStream.json[GeoResult](bais)
      in.iterator.toList
    }
  }

  private def buildKey(location: String) = {
    val suffix = StringUtils.stripAccents(location.toLowerCase().toCharArray.filter(_.isLetterOrDigit).mkString)
    s"$prefix:$suffix"
  }
}
