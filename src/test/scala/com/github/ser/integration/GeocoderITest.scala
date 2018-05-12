package com.github.ser.integration

import com.github.ser._
import com.github.ser.test.Randoms
import com.redis.RedisClient
import org.apache.spark.SparkContext
import org.scalatest.{FunSuite, Matchers}

class GeocoderITest(sc: SparkContext, redis: RedisClient) extends FunSuite with Matchers with Randoms {
  test("should read data from geocoder") {
    val reader = new Reader(sc)
    val cleaner = new Cleaner(sc)
    val geocoder = new Geocoder(sc, "https://nominatim.openstreetmap.org", new RedisGeoResultCache(redis, s"geocoderITest:${randomString(5)}"))

    val users = Seq(
      cleaner.cleanUsers _,
      geocoder.fetchGeoResults _
    ).reduce(_ andThen _)(reader.loadUsers(this.getClass.getClassLoader.getResource("Users_2.xml").getPath)).collect().toList

    users.foreach(_.geoResults should not be empty)
  }
}
