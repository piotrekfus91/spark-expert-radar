package com.github.ser.test

import com.github.ser.integration.GeocoderITest
import org.scalatest.Suites

class IntegrationTests extends Suites(

  new GeocoderITest(Spark.sc)

)


