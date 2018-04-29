package com.github.ser

import com.github.ser.domain.User
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD

class Cleaner(val sc: SparkContext) {
  def cleanUsers(users: RDD[User]): RDD[User] = {
    List(
      removeSpecialUsers,
      removeWithoutLocation
    ).reduce(_ andThen _)(users)
  }

  private val removeSpecialUsers = (users: RDD[User]) => users.filter(_.id > 0)
  private val removeWithoutLocation = (users: RDD[User]) => users.filter(_.location.isDefined)
}
