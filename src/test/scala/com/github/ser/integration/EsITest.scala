package com.github.ser.integration

import com.github.ser.setup.ElasticsearchSetup
import com.github.ser.test.Index
import com.github.ser.{Cleaner, EsSaver, Geocoder, Reader}
import com.sksamuel.elastic4s.http.HttpClient
import com.sksamuel.elastic4s.http.ElasticDsl._
import org.apache.spark.SparkContext
import org.scalatest.{FunSuite, Matchers}

class EsITest(sc: SparkContext, client: HttpClient) extends FunSuite with Matchers {
  test("save data to es") {
    val esSetup = new ElasticsearchSetup(client)
    esSetup.setupIndex(Index.indexName, Index.rawMapping)

    val reader = new Reader(sc)
    val cleaner = new Cleaner(sc)
    val geocoder = new Geocoder(sc, "https://nominatim.openstreetmap.org")
    val esSaver = new EsSaver(sc)

    val users = Seq(
      cleaner.cleanUsers _,
      geocoder.fetchGeoResults _,
      esSaver.saveUsersInEs _
    ).reduce(_ andThen _)(reader.loadUsers(this.getClass.getClassLoader.getResource("Users_2.xml").getPath)).collect().toList

    val response = client.execute {
      search(Index.indexName) query "Jeff"
    }.await

    response match {
      case Left(s) => fail(s"error while contacting with ES: $s")
      case Right(r) =>
        r.result.hits.size shouldBe 1
        val hit = r.result.hits.hits(0)
        hit.sourceAsMap("userId") shouldBe 1
        hit.sourceAsMap("displayName") shouldBe "Jeff Atwood"
        hit.sourceAsMap("location") shouldBe "El Cerrito, CA"
        hit.sourceAsMap("geolocation") shouldBe "37.9154056,-122.301411"
    }
  }
}
