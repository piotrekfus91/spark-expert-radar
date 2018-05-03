package com.github.ser.setup

import com.sksamuel.elastic4s.http.ElasticDsl._
import com.sksamuel.elastic4s.http.HttpClient

class ElasticsearchSetup(client: HttpClient) {
  def setupIndex(indexName: String, sourceJson: String): Unit = {
    println(s"index name: $indexName")
    client execute {
      createIndex(indexName) source sourceJson
    }
  }

  def removeIndex(indexPrefix: String): Unit = {
    client execute {
      deleteIndex(s"$indexPrefix*")
    }
  }
}
