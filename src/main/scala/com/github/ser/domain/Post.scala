package com.github.ser.domain

import com.typesafe.scalalogging.LazyLogging

import scala.collection.mutable

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
case class Other(value: Int) extends PostType

object PostType extends LazyLogging {
  val otherCache = mutable.Map[String, Other]().withDefault { value => Other(value.toInt) }

  def apply(in: String) = in match {
    case "1" => Question
    case "2" => Answer
    case other => otherCache(other)
  }
}
