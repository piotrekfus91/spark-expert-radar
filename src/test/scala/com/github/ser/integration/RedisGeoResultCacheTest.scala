package com.github.ser.integration

import com.github.ser.RedisGeoResultCache
import com.github.ser.domain.{BoundingBox, GeoResult}
import com.github.ser.test.Randoms
import com.redis.RedisClient
import org.scalatest.{FlatSpec, Matchers}

class RedisGeoResultCacheTest(redis: RedisClient) extends FlatSpec with Matchers with Randoms {
  val sut = new RedisGeoResultCache(redis, randomString(10))

  "Redis cache" should "save empty list" in {
    sut.save("simple location", List.empty)

    sut.get("simple location") shouldBe Some(List.empty)
  }

  it should "save list" in {
    sut.save("single location", List(GeoResult("disp", 12, 34, 56, BoundingBox(11, 22, 33, 44))))

    sut.get("single location") should contain(List(GeoResult("disp", 12, 34, 56, BoundingBox(11, 22, 33, 44))))
  }

  it should "return None if key not found" in {
    sut.get("not exists") shouldBe None
  }

  it should "return same list for similar locations" in {
    val list = List(GeoResult("disp", 12, 34, 56, BoundingBox(11, 22, 33, 44)))
    sut.save("single location", list)

    Seq(
      "single    location",
      "single,location",
      "Single Location",
      "Single-Location"
    ).foreach(sut.get(_) shouldBe Some(list))
  }

  it should "return same value for accents" in {
    val list = List(GeoResult("disp", 12, 34, 56, BoundingBox(11, 22, 33, 44)))
    sut.save("zażółć gęślą jaźń", list)

    Seq(
      "zażółć gęślą jaźń",
      "zażółć,gęślą jaźń",
      "zazolc gesla jazn"
    ).foreach(sut.get(_) shouldBe Some(list))
  }
}
