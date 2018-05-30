package com.github.ser

import com.github.ser.domain.{Post, PostType, User}
import org.apache.spark.sql.{Encoders, SparkSession}

trait SparkProvider {
  val spark = SparkSession.builder()
    .master("local[*]")
    .appName(this.getClass.getSimpleName)
    .getOrCreate()
  val sqlContext = spark.sqlContext
  val conf = spark.conf

  implicit val userEncoder = Encoders.kryo[User]
  implicit val postEncoder = Encoders.kryo[Post]
  implicit val postTypeEncoder = Encoders.kryo[PostType]
}
