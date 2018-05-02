package com.github.ser.integration

import com.github.ser.{Cleaner, Geocoder, Reader}
import org.apache.spark.SparkContext
import org.scalatest.{FunSuite, Matchers}

class GeocoderITest(sc: SparkContext) extends FunSuite with Matchers {
  test("should read data from geocoder") {
    val reader = new Reader(sc)
    val cleaner = new Cleaner(sc)
    val geocoder = new Geocoder(sc, "https://nominatim.openstreetmap.org")

    val users = Seq(
      cleaner.cleanUsers _,
      geocoder.fetchGeoResults _
    ).reduce(_ andThen _)(reader.loadUsers(this.getClass.getClassLoader.getResource("Users_2.xml").getPath)).collect().toList

    println(users)
    users.foreach(_.geoResults should not be empty)
  }
}
