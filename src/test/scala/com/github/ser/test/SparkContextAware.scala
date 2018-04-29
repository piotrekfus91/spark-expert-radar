package com.github.ser.test

import org.apache.spark.{SparkConf, SparkContext}
import org.scalatest.{BeforeAndAfterAll, Suite}

trait SparkContextAware extends BeforeAndAfterAll {
  this: Suite =>

  val sparkConf = new SparkConf()
    .setAppName(this.getClass.getSimpleName)
    .setMaster("local[*]")

  val sc = new SparkContext(sparkConf)

  override protected def afterAll(): Unit = {
    super.afterAll()
    sc.stop()
  }
}
