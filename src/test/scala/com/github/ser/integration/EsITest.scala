package com.github.ser.integration

import com.github.ser.setup.ElasticsearchSetup
import com.github.ser.test.Index
import com.github.ser._
import com.sksamuel.elastic4s.http.HttpClient
import com.sksamuel.elastic4s.http.ElasticDsl._
import org.apache.spark.SparkContext
import org.scalatest.{BeforeAndAfterAll, FunSuite, Matchers}

class EsITest(sc: SparkContext, client: HttpClient) extends FunSuite with Matchers with BeforeAndAfterAll {
  val reader = new Reader(sc)
  val cleaner = new Cleaner(sc)
  val geocoder = new Geocoder(sc, "https://nominatim.openstreetmap.org", new MapBasedGeoResultCache)
  val esSaver = new EsSaver(sc)

  override protected def beforeAll(): Unit = {
    new ElasticsearchSetup(client).setupIndex(Index.userIndex, Index.userMapping)
    new ElasticsearchSetup(client).setupIndex(Index.postIndex, Index.postMapping)
  }

  test("save users to es") {
    val users = Seq(
      cleaner.cleanUsers _,
      geocoder.fetchGeoResults _,
      esSaver.saveUsersInEs _
    ).reduce(_ andThen _)(reader.loadUsers(this.getClass.getClassLoader.getResource("Users_2.xml").getPath)).collect().toList

    val response = client.execute {
      search(Index.userIndex) query "Jeff"
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

  test("save posts to es") {
    val posts = Seq(
      esSaver.savePostsInEs _
    ).reduce(_ andThen _)(reader.loadPosts(this.getClass.getClassLoader.getResource("Posts_2.xml").getPath)).collect().toList

    val response = client.execute {
      search(Index.postIndex) query "winforms"
    }.await

    response match {
      case Left(s) => fail(s"error while contacting with ES: $s")
      case Right(r) =>
        r.result.hits.size shouldBe 1
        val hit = r.result.hits.hits(0)
        hit.sourceAsMap("postId") shouldBe 4
        hit.sourceAsMap("parentId") shouldBe null
        hit.sourceAsMap("postType") shouldBe "Question"
        hit.sourceAsMap("score") shouldBe 543
        hit.sourceAsMap("tags") shouldBe List("c#", "winforms", "type-conversion", "decimal", "opacity")
    }
  }
}
