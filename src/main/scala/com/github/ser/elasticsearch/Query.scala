package com.github.ser.elasticsearch

import com.github.ser.domain.{Point, Post, PostType, User}
import com.sksamuel.elastic4s.http.ElasticDsl._
import com.sksamuel.elastic4s.http.HttpClient
import com.sksamuel.elastic4s.http.search.{SearchHit, SearchHits}

class Query(client: HttpClient, indexPrefix: String) {
  private val userIndex = s"$indexPrefix-user"
  private val postIndex = s"$indexPrefix-post"

  def queryUsersSingle(query: String): Option[User] = {
    val response = client.execute {
      search(userIndex) query query
    }.await

    response match {
      case Left(s) => throw new RuntimeException(s"error while contacting with ES: $s")
      case Right(r) =>
        verifySingleResult(query, r.result.hits)
        if (r.result.hits.size > 0)
          Some(mapToUser(r.result.hits.hits(0)))
        else
          None
    }
  }

  private def mapToUser(searchHit: SearchHit) = User(
    searchHit.sourceAsMap("userId").asInstanceOf[Int].toLong,
    searchHit.sourceAsMap("displayName").asInstanceOf[String],
    searchHit.sourceAsMap.get("location").map(_.asInstanceOf[String]).flatMap(s => if (s == "") None else Option(s)),
    searchHit.sourceAsMap("reputation").asInstanceOf[Int].toLong,
    searchHit.sourceAsMap("upvotes").asInstanceOf[Int].toLong,
    searchHit.sourceAsMap("downvotes").asInstanceOf[Int].toLong
  ).copy(points = mapToPoints(searchHit.sourceAsMap("points").asInstanceOf[List[Map[String, Object]]]))

  private def mapToPoints(points: List[Map[String, Object]]): List[Point] =
    points.map(point => Point(point("postId").asInstanceOf[Int], point("tag").asInstanceOf[String], point("score").asInstanceOf[Int]))

  def queryPostsSingle(query: String): Option[Post] = {
    val response = client.execute {
      search(postIndex) query query
    }.await

    response match {
      case Left(s) => throw new RuntimeException(s"error while contacting with ES: $s")
      case Right(r) =>
        verifySingleResult(query, r.result.hits)
        if (r.result.hits.size > 0)
          Some(mapToPost(r.result.hits.hits(0)))
        else
          None
    }
  }

  private def verifySingleResult(query: String, searchHits: SearchHits) = {
    if (searchHits.size > 1) {
      throw new RuntimeException(s"wrong number of results: ${searchHits.size} for query: $query")
    }
  }

  private def mapToPost(searchHit: SearchHit) = Post(
    searchHit.sourceAsMap("postId").asInstanceOf[Int].toLong,
    optionalLong(searchHit, "parentId"),
    PostType.fromName(searchHit.sourceAsMap("postType").asInstanceOf[String]),
    searchHit.sourceAsMap("score").asInstanceOf[Int].toLong,
    optionalLong(searchHit, "ownerUserId"),
    searchHit.sourceAsMap("tags").asInstanceOf[List[String]]
  )

  private def optionalLong(searchHit: SearchHit, field: String) = {
    if (searchHit.sourceAsMap(field).asInstanceOf[Int] == 0)
      None
    else
      Some(searchHit.sourceAsMap(field).asInstanceOf[Int].toLong)
  }
}
