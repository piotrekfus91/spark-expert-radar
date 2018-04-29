package com.github.ser

import com.github.ser.domain.User
import org.apache.spark.SparkContext
import org.scalatest.{Matchers, WordSpec}

class CleanerTest(sc: SparkContext) extends WordSpec with Matchers {

  val technicalUser = User(-1, "technical", Some("Somewhere"))
  val normalUser = User(1, "normal user", Some("Warsaw"))
  val homelessUser = User(1, "homeless user", None)
  val userList = List(technicalUser, normalUser, homelessUser)
  val sut = new Cleaner(sc)

  "Cleaner" when {
    "clean users" should {
      val cleanedUsers = sut.cleanUsers(sc.parallelize(userList)).collect()

      "contain normal user" in {
        cleanedUsers should contain(normalUser)
      }

      "not contain technical user" in {
        cleanedUsers should not contain(technicalUser)
      }

      "not contain homeless user" in {
        cleanedUsers should not contain(homelessUser)
      }
    }
  }
}
