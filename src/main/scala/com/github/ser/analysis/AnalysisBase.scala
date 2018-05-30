package com.github.ser.analysis

import com.github.ser.{Reader, SparkProvider}

trait AnalysisBase extends SparkProvider {
  val reader = new Reader()
}
