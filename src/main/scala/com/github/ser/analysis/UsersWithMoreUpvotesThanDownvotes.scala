package com.github.ser.analysis

object UsersWithMoreUpvotesThanDownvotes extends AnalysisBase with App {
  val moreUpvotesThanDownvotes = reader.loadUsers("/home/pfus/Studia/bigdata/projekt/data/Users.xml")
    .filter(u => u.upvotes > u.downvotes)
    .count()

  println(s"More upvotes than downvotes: $moreUpvotesThanDownvotes")
}
