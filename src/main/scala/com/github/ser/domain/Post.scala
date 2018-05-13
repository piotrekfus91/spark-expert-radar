package com.github.ser.domain

case class Post(
                id: Long,
                parentId: Option[Long],
                postType: PostType,
                score: Long,
                tags: List[String]
               )

sealed trait PostType {
  def name = this.getClass.getSimpleName.dropRight(1)
}
case object Question extends PostType
case object Answer extends PostType

object PostType {
  def apply(in: String) = in match {
    case "1" => Question
    case "2" => Answer
    case _ => throw new RuntimeException(s"unknown post type: $in")
  }
}
