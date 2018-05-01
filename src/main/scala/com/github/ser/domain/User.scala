package com.github.ser.domain

case class User(id: Long, displayName: String, location: Option[String], geoResults: List[GeoResult] = List.empty)
