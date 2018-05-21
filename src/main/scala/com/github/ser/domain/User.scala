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
                ) {
  val escapedTag = {
    val firstNonDotPosition = tag.zipWithIndex.find(_._1 != '.').map(_._2).getOrElse(throw new RuntimeException(s"tag [$tag] contains dots only or is empty"))
    val correctPrefix = tag.drop(firstNonDotPosition)
    val lastNonDotPosition = correctPrefix.reverse.zipWithIndex.find(_._1 != '.').map(_._2).get
    correctPrefix.dropRight(lastNonDotPosition).replace(".", "_")
  }
}
