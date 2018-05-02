package com.github.ser.test

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.core.WireMockConfiguration

object WireMock {
  val wm = new WireMockServer(WireMockConfiguration.wireMockConfig().port(3737))
}
