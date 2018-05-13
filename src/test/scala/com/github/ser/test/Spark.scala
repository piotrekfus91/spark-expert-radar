package com.github.ser.test

import org.apache.spark.{SparkConf, SparkContext}

object Spark {
  val sparkConf = new SparkConf()
    .setAppName(this.getClass.getSimpleName)
    .setMaster("local[*]")
    .set("geocoding.host", "http://localhost:3737")
    .set("es.index", Index.indexPrefix)

  val sc = new SparkContext(sparkConf)
}
