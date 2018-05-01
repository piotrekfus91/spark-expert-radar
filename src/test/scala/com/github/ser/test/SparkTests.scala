package com.github.ser.test

import com.github.ser.{CleanerTest, GeocoderTest, ReaderTest}
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import org.apache.spark.{SparkConf, SparkContext}
import org.scalatest.{BeforeAndAfterAll, Suites}

class SparkTests extends Suites(

  new ReaderTest(Spark.sc),
  new CleanerTest(Spark.sc),
  new GeocoderTest(Spark.sc, WireMock.wm)

) with BeforeAndAfterAll {

  override protected def beforeAll(): Unit = WireMock.wm.start()

  override protected def afterAll(): Unit = WireMock.wm.stop()
}

object Spark {
  val sparkConf = new SparkConf()
    .setAppName(this.getClass.getSimpleName)
    .setMaster("local[*]")
    .set("geocoding.host", "http://localhost:3737")

  val sc = new SparkContext(sparkConf)
}

object WireMock {
  val wm = new WireMockServer(WireMockConfiguration.wireMockConfig().port(3737))
}
