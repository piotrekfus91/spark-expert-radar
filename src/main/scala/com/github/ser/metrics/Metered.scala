package com.github.ser.metrics

import java.util.concurrent.Callable

import io.micrometer.core.instrument.Metrics

object Metered {
  def timed[A](metricName: String, tags: String*)(f: () => A) = {
    Metrics.timer(metricName, tags: _*).recordCallable(new Callable[A] {
      override def call(): A = f()
    })
  }
}
