package com.github.ser.analysis

import com.github.ser.Reader
import org.apache.spark.{SparkConf, SparkContext}

trait AnalysisBase {
  val sparkConf = new SparkConf()
    .setAppName(this.getClass.getSimpleName)
    .setMaster("local[*]")

  val sc = new SparkContext(sparkConf)

  val reader = new Reader(sc)
}
