package com.github.ser

import com.github.ser.domain.User
import com.github.ser.test.SparkContextAware
import org.scalatest.{FlatSpec, Matchers}

class ReaderTest extends FlatSpec with SparkContextAware with Matchers {
  val sut = new Reader(sc)

  it should "read file to RDD" in {
    val users = sut.loadUsers(this.getClass.getClassLoader.getResource("Users_2.xml").getPath).collect()

    users should contain allOf(
      User(1, "Jeff Atwood", Some("El Cerrito, CA")),
      User(2, "Geoff Dalgas", None)
    )
  }
}
