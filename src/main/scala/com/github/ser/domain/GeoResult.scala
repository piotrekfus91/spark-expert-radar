package com.github.ser.domain

case class GeoResult(displayName: String, latitude: Double, longitude: Double, importance: Double, boundingBox: BoundingBox)
case class BoundingBox(westLatitude: Double, eastLatitude: Double, northLongitude: Double, southLongitude: Double)
