package com.github.ser.setup

import com.sksamuel.elastic4s.http.ElasticDsl._
import com.sksamuel.elastic4s.http.HttpClient
import com.typesafe.scalalogging.LazyLogging

class ElasticsearchSetup(client: HttpClient) extends LazyLogging {
  def setupIndex(indexName: String, sourceJson: String): Unit = {
    logger.info(s"creating index $indexName")
    client execute {
      createIndex(indexName) source sourceJson
    }
  }

  def removeIndex(indexPrefix: String): Unit = {
    logger.info(s"removing index with prefix $indexPrefix")
    client execute {
      deleteIndex(s"$indexPrefix*")
    }
  }
}
