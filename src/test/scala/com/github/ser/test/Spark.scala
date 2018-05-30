package com.github.ser.test

import com.github.ser.domain.{Post, PostType, User}
import org.apache.spark.sql.{Encoders, SparkSession}

trait Spark {
  val spark = SparkSession.builder()
    .appName(this.getClass.getSimpleName)
    .master("local[*]")
    .config("geocoding.host", "http://localhost:3737")
    .config("es.index", Index.indexPrefix)
    .getOrCreate()

  val sc = spark.sparkContext

  implicit val postTypeEncoder = Encoders.kryo[PostType]
  implicit val postEncoder = Encoders.kryo[Post]
  implicit val userEncoder = Encoders.kryo[User]
}
