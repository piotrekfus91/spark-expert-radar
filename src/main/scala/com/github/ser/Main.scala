package com.github.ser

import com.github.ser.setup.ElasticsearchSetup
import com.sksamuel.elastic4s.ElasticsearchClientUri
import com.sksamuel.elastic4s.http.HttpClient
import org.apache.spark.{SparkConf, SparkContext}

import scala.io.Source

object Main extends App {
  val usersPath = args(0)

  val esClient = HttpClient(ElasticsearchClientUri("localhost", 9200))
  val indexName = "users"

  val esSetup = new ElasticsearchSetup(esClient)
  esSetup.removeIndex(indexName) // TODO
  esSetup.setupIndex(indexName, Source.fromFile(Main.getClass.getClassLoader.getResource("elasticsearch/index.json").getPath).mkString)

  val sparkConf = new SparkConf()
    .setAppName(this.getClass.getSimpleName)
    .setMaster("local[*]")
    .set("es.index", indexName)

  val sc = new SparkContext(sparkConf)

  val reader = new Reader(sc)
  val cleaner = new Cleaner(sc)
  val geocoder = new Geocoder(sc, "https://nominatim.openstreetmap.org")
  val esSaver = new EsSaver(sc)

  val users = Seq(
    cleaner.cleanUsers _,
    geocoder.fetchGeoResults _,
    esSaver.saveUsersInEs _
  ).reduce(_ andThen _)(reader.loadUsers(usersPath))

  println(s"Indexing finished, indexed: ${users.count()}")

  sc.stop()
}
