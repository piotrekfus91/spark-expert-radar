package com.github.ser.test

import com.sksamuel.elastic4s.ElasticsearchClientUri
import com.sksamuel.elastic4s.http.HttpClient

object Elasticsearch {
  val client = HttpClient(ElasticsearchClientUri("localhost", 9200))
}
