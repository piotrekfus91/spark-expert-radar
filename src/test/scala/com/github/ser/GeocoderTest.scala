package com.github.ser

import com.github.ser.domain.{BoundingBox, GeoResult, User}
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock._
import org.apache.spark.SparkContext
import org.scalatest.{Matchers, WordSpec}

class GeocoderTest(sc: SparkContext, wireMock: WireMockServer) extends WordSpec with Matchers {
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

  val sut = new Geocoder(sc, "http://localhost:3737")

  "Geocoder" when {
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
}
