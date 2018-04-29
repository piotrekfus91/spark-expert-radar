package com.github.ser.test

import com.github.ser.{CleanerTest, ReaderTest}
import org.apache.spark.{SparkConf, SparkContext}
import org.scalatest.Suites

class AllTests extends Suites(new ReaderTest(Spark.sc), new CleanerTest(Spark.sc))

object Spark {
  val sparkConf = new SparkConf()
    .setAppName(this.getClass.getSimpleName)
    .setMaster("local[*]")

  val sc = new SparkContext(sparkConf)
}
