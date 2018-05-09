package com.github.ser.domain

case class User(
                 id: Long,
                 displayName: String,
                 location: Option[String],
                 reputations: Long = 0,
                 upvotes: Long = 0,
                 downvotes: Long = 0,
                 geoResults: List[GeoResult] = List.empty
               )
