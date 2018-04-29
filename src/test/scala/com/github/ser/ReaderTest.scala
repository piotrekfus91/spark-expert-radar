package com.github.ser

import com.github.ser.domain.User
import org.apache.spark.SparkContext
import org.scalatest.{FlatSpec, Matchers}

class ReaderTest(sc: SparkContext) extends FlatSpec with Matchers {
  val sut = new Reader(sc)

  it should "read file to RDD" in {
    val users = sut.loadUsers(this.getClass.getClassLoader.getResource("Users_2.xml").getPath).collect()

    users should contain allOf(
      User(1, "Jeff Atwood", Some("El Cerrito, CA")),
      User(2, "Geoff Dalgas", None)
    )
  }
}
