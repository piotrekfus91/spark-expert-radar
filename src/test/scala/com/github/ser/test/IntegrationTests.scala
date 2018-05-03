package com.github.ser.test

import com.github.ser.integration.{EsITest, GeocoderITest}
import com.github.ser.setup.ElasticsearchSetup
import org.scalatest.{BeforeAndAfterAll, Suites}

class IntegrationTests extends Suites(

  new GeocoderITest(Spark.sc),
  new EsITest(Spark.sc, Elasticsearch.client)

) with BeforeAndAfterAll {

  override protected def beforeAll(): Unit = new ElasticsearchSetup(Elasticsearch.client).removeIndex(Index.indexPrefix)

}
