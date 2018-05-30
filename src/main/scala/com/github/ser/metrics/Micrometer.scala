package com.github.ser.metrics

import java.time.Duration

import io.micrometer.core.instrument.Clock
import io.micrometer.influx.{InfluxConfig, InfluxMeterRegistry}

object Micrometer {
  private val influxConfig = new InfluxConfig {
    override def step(): Duration = Duration.ofMillis(1000)

    override def db(): String = "serdb"

    override def get(key: String): String = null
  }

  val meterRegistry = new InfluxMeterRegistry(influxConfig, Clock.SYSTEM)
}
