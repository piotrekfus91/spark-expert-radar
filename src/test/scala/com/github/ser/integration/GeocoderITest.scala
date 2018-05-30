package com.github.ser.integration

import com.github.ser._
import com.github.ser.test.{Google, Randoms, Spark}
import com.redis.RedisClient
import org.scalatest.{FunSuite, Matchers}

class GeocoderITest(redis: RedisClient) extends FunSuite with Matchers with Randoms with Spark {

  test("should read data from nominatim") {
    val reader = new Reader()
    val cleaner = new Cleaner()
    val geocoder = new Geocoder("https://nominatim.openstreetmap.org", new EmptyGeoResultCache, new NominatimGeoEngine)

    val users = Seq(
      cleaner.cleanUsers _,
      geocoder.fetchGeoResults _
    ).reduce(_ andThen _)(reader.loadUsers(this.getClass.getClassLoader.getResource("Users_2.xml").getPath)).collect().toList

    users.foreach(_.geoResults should not be empty)
  }

  test("should read data from google") {
    val reader = new Reader()
    val cleaner = new Cleaner()
    val geocoder = new Geocoder("https://maps.googleapis.com", new EmptyGeoResultCache, new GoogleGeoEngine(Google.apiKey))

    val users = Seq(
      cleaner.cleanUsers _,
      geocoder.fetchGeoResults _
    ).reduce(_ andThen _)(reader.loadUsers(this.getClass.getClassLoader.getResource("Users_2.xml").getPath)).collect().toList

    users.foreach(_.geoResults should not be empty)
  }
}
