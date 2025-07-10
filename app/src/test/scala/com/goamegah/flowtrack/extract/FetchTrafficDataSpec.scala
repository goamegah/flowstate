package com.goamegah.flowtrack.extract


import scala.concurrent.duration._
import org.scalatest.concurrent.TimeLimits._
import org.scalatest.time.SpanSugar._

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class FetchTrafficDataSpec extends AnyFlatSpec with Matchers {

  "FetchTrafficData" should "return data for a valid public API endpoint" in {
    val result = FetchTrafficData.fetchData(Some("https://data.rennesmetropole.fr/api/explore/v2.1/catalog/datasets/etat-du-trafic-en-temps-reel/records?limit=1"))
    result should not be empty
  }

  it should "return None for an invalid endpoint" in {
    val result = FetchTrafficData.fetchData(Some("https://fake.domain.404/endpoint"))
    result shouldBe None
  }

  it should "use the default endpoint if none is provided" in {
    val result = FetchTrafficData.fetchData()
    result should not be empty
  }

  //it should "return data within a timeout" in {
  //  failAfter(5.seconds) {
  //    val result = FetchTrafficData.fetchData()
  //    result should not be empty
  //  }
  //}
}

