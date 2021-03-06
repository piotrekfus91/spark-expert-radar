package com.github.ser.test

import com.github.ser._
import org.scalatest.{BeforeAndAfterAll, Suites}

class SparkTests extends Suites(

  new ReaderTest(),
  new CleanerTest(),
  new NominatimGeoCodingTest(WireMock.wm),
  new GoogleGeoCodingTest(WireMock.wm)

) with BeforeAndAfterAll {

  override protected def beforeAll(): Unit = WireMock.wm.start()

  override protected def afterAll(): Unit = WireMock.wm.stop()
}
