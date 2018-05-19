package com.github.ser.domain

case class User(
                 id: Long,
                 displayName: String,
                 location: Option[String],
                 reputation: Long = 0,
                 upvotes: Long = 0,
                 downvotes: Long = 0,
                 geoResults: List[GeoResult] = List.empty,
                 points: List[Point] = List.empty
               )

case class Point(
                  postId: Long,
                  tag: String,
                  score: Long
                )
