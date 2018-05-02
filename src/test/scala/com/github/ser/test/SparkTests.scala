package com.github.ser.test

import com.github.ser.{CleanerTest, GeocoderTest, ReaderTest}
import org.scalatest.{BeforeAndAfterAll, Suites}

class SparkTests extends Suites(

  new ReaderTest(Spark.sc),
  new CleanerTest(Spark.sc),
  new GeocoderTest(Spark.sc, WireMock.wm)

) with BeforeAndAfterAll {

  override protected def beforeAll(): Unit = WireMock.wm.start()

  override protected def afterAll(): Unit = WireMock.wm.stop()
}
