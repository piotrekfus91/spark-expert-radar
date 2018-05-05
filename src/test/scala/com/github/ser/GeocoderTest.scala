package com.github.ser

import com.github.ser.domain.{BoundingBox, GeoResult, User}
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock._
import org.apache.spark.SparkContext
import org.scalamock.scalatest.MockFactory
import org.scalatest.{Matchers, WordSpec}

class GeocoderTest(sc: SparkContext, wireMock: WireMockServer) extends WordSpec with Matchers with MockFactory {
  val location1 =
    """
      |{
      |    "place_id": "177749604",
      |    "licence": "Data © OpenStreetMap contributors, ODbL 1.0. https:\/\/osm.org\/copyright",
      |    "osm_type": "relation",
      |    "osm_id": "336074",
      |    "boundingbox": [
      |      "52.0978507",
      |      "52.3681531",
      |      "20.8516882",
      |      "21.2711512"
      |    ],
      |    "lat": "52.2319237",
      |    "lon": "21.0067265",
      |    "display_name": "Warsaw, Warszawa, Masovian Voivodeship, Poland",
      |    "class": "place",
      |    "type": "city",
      |    "importance": 0.41072546160754,
      |    "icon": "https:\/\/nominatim.openstreetmap.org\/images\/mapicons\/poi_place_city.p.20.png"
      |}""".stripMargin

  val location2 =
    """
      |{
      |    "place_id": "179830881",
      |    "licence": "Data © OpenStreetMap contributors, ODbL 1.0. https:\/\/osm.org\/copyright",
      |    "osm_type": "relation",
      |    "osm_id": "3490160",
      |    "boundingbox": [
      |      "52.1536005",
      |      "52.1543307",
      |      "20.9952683",
      |      "20.9959734"
      |    ],
      |    "lat": "52.1539925",
      |    "lon": "20.9956212354654",
      |    "display_name": "Warsaw, 12, Osmańska, Krasnowola, Ursynów, Warsaw, Warszawa, Masovian Voivodeship, 02-823, Poland",
      |    "class": "building",
      |    "type": "yes",
      |    "importance": 0.22025
      |  }
    """.stripMargin

  val geocoderSortedResponse = s"[$location1, $location2]"
  val geocoderUnsortedResponse = s"[$location2, $location1]"

  val users = List(User(123, "some user", Some("Warsaw")))
  val usersRdd = sc.parallelize(users)

  val expectedUser = User(123, "some user", Some("Warsaw"), List(
    GeoResult("Warsaw, Warszawa, Masovian Voivodeship, Poland", 52.2319237, 21.0067265, 0.41072546160754, BoundingBox(52.0978507, 52.3681531, 20.8516882, 21.2711512)),
    GeoResult("Warsaw, 12, Osmańska, Krasnowola, Ursynów, Warsaw, Warszawa, Masovian Voivodeship, 02-823, Poland", 52.1539925, 20.9956212354654, 0.22025, BoundingBox(52.1536005, 52.1543307, 20.9952683, 20.9959734))
  ))

  "Geocoder without cache" when {
    val sut = new Geocoder(sc, "http://localhost:3737", new MapBasedGeoResultCache)

    "read correct geocoding data" should {
      "if data are sorted" in {
        wireMock.stubFor(
          get(urlPathMatching("/search"))
            .willReturn(aResponse().withBody(geocoderSortedResponse))
        )
        val usersWithGeoResults = sut.fetchGeoResults(usersRdd).collect()
        usersWithGeoResults should contain(expectedUser)
      }

      "if data are unsorted" in {
        wireMock.stubFor(
          get(urlPathMatching("/search"))
            .willReturn(aResponse().withBody(geocoderUnsortedResponse))
        )

        val usersWithGeoResults = sut.fetchGeoResults(usersRdd).collect()
        usersWithGeoResults should contain(expectedUser)
      }

      "if parameters are correct" in {
        wireMock.stubFor(
          get(urlPathMatching("/search"))
            .withQueryParam("q", equalTo("Warsaw"))
            .withQueryParam("format", equalTo("json"))
            .willReturn(aResponse().withBody(geocoderUnsortedResponse))
        )

        val usersWithGeoResults = sut.fetchGeoResults(usersRdd).collect()
        usersWithGeoResults should contain(expectedUser)
      }
    }

    "return empty list" should {
      "if response is not json" in {
        wireMock.stubFor(
          get(urlPathMatching("/search"))
            .willReturn(aResponse().withBody("this is not JSON"))
        )

        val usersWithGeoResults = sut.fetchGeoResults(usersRdd).collect()
        usersWithGeoResults should contain(User(123,"some user",Some("Warsaw"), List()))
      }

      "if user has no location" in {
        wireMock.stubFor(
          get(urlPathMatching("/search"))
            .willReturn(aResponse().withBody(geocoderSortedResponse))
        )

        val usersWithGeoResults = sut.fetchGeoResults(sc.parallelize(Seq(User(1, "homeless user", None)))).collect()
        usersWithGeoResults should contain(User(1,"homeless user", None, List()))
      }
    }
  }

  "Geocoder with mock cache" when {
    val cache = new MapBasedGeoResultCache
    val sut = new Geocoder(sc, "http://localhost:3737", cache)

    "use cache correctly" should {
      "use geocoder if key not exists in cache" in {
        wireMock.stubFor(
          get(urlPathMatching("/search"))
            .willReturn(aResponse().withBody(geocoderSortedResponse))
        )
        val usersRdd = sc.parallelize(Seq(User(1, "some user", Some("single"))))

        val usersWithGeoResults = sut.fetchGeoResults(usersRdd).collect()
        usersWithGeoResults(0).geoResults should not be empty

        wireMock.verify(1, getRequestedFor(urlPathMatching("/search")).withQueryParam("q", equalTo("single")))
      }

      "not use geocoder if key exists in cache" in {
        wireMock.stubFor(
          get(urlPathMatching("/search"))
            .willReturn(aResponse().withBody(geocoderSortedResponse))
        )
        cache.save("otherSingle", List.empty)
        val usersRdd = sc.parallelize(Seq(User(1, "some user", Some("otherSingle"))))

        sut.fetchGeoResults(usersRdd).collect()

        wireMock.verify(0, getRequestedFor(urlPathMatching("/search")).withQueryParam("q", equalTo("otherSingle")))
      }
    }
  }
}
