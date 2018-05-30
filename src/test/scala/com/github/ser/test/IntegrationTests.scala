package com.github.ser.test

import com.github.ser.integration.{EsITest, GeocoderITest, RedisGeoResultCacheTest}
import com.github.ser.elasticsearch.ElasticsearchSetup
import org.scalatest.{BeforeAndAfterAll, Suites}

class IntegrationTests extends Suites(

  new GeocoderITest(Redis.client),
  new EsITest(Elasticsearch.client),
  new RedisGeoResultCacheTest(Redis.client)

) with BeforeAndAfterAll {

  override protected def beforeAll(): Unit = new ElasticsearchSetup(Elasticsearch.client).removeIndex(Index.indexRemovalPrefix)

}
