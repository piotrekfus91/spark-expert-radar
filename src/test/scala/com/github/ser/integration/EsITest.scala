package com.github.ser.integration

import com.github.ser._
import com.github.ser.domain.{Point, Post, Question, User}
import com.github.ser.elasticsearch.{ElasticsearchSetup, Query}
import com.github.ser.test.Index
import com.sksamuel.elastic4s.http.ElasticDsl._
import com.sksamuel.elastic4s.http.HttpClient
import org.apache.spark.SparkContext
import org.scalatest.{BeforeAndAfterAll, FunSuite, Matchers}

class EsITest(sc: SparkContext, client: HttpClient) extends FunSuite with Matchers with BeforeAndAfterAll {
  val reader = new Reader(sc)
  val cleaner = new Cleaner(sc)
  val geocoder = new Geocoder(sc, "https://nominatim.openstreetmap.org", new MapBasedGeoResultCache)
  val esSaver = new EsSaver(sc)
  val query = new Query(client, Index.indexPrefix)

  override protected def beforeAll(): Unit = {
    new ElasticsearchSetup(client).setupIndex(Index.userIndex, Index.userMapping)
    new ElasticsearchSetup(client).setupIndex(Index.postIndex, Index.postMapping)
  }

  test("save users to es") {
    Seq(
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
    Seq(
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
        hit.sourceAsMap("ownerUserId") shouldBe 8
        hit.sourceAsMap("tags") shouldBe List("c#", "winforms", "type-conversion", "decimal", "opacity")
    }

    query.queryPostsSingle("tags:winforms") shouldBe Some(Post(4, None, Question, 543, Some(8), List("c#", "winforms", "type-conversion", "decimal", "opacity")))
  }

  test("save user with points") {
    val user = User(345, "UserWithPoints", None).copy(points = List(Point(567, "Java", 123), Point(765, "Scala", -321)))
    esSaver.saveUsersInEs(sc.parallelize(List(user)))

    val response = client.execute {
      search(Index.userIndex) query "displayName:UserWithPoints"
    }.await

    response match {
      case Left(s) => fail(s"error while contacting with ES: $s")
      case Right(r) =>
        r.result.hits.size shouldBe 1
        val hit = r.result.hits.hits(0)
        hit.sourceAsMap("userId") shouldBe 345
        hit.sourceAsMap("displayName") shouldBe "UserWithPoints"
        hit.sourceAsMap("points") shouldBe List(
          Map("postId" -> 567, "tag" -> "Java", "score" -> 123),
          Map("postId" -> 765, "tag" -> "Scala", "score" -> -321)
        )
        hit.sourceAsMap("scoresSum") shouldBe Map(
          "Java" -> 123,
          "Scala" -> -321
        )
        hit.sourceAsMap("scoresAvg") shouldBe Map(
          "Java" -> 123,
          "Scala" -> -321
        )
        hit.sourceAsMap("scoresCount") shouldBe Map(
          "Java" -> 1,
          "Scala" -> 1
        )
    }

    query.queryUsersSingle("displayName:UserWithPoints") shouldBe Some(user)
  }
}
