package gatling.simulation

import io.gatling.core.Predef._
import io.gatling.core.structure.ChainBuilder
import io.gatling.http.Predef._
import io.gatling.http.protocol.HttpProtocolBuilder

import scala.concurrent.duration._

class LoadTest extends Simulation {

  before {
    println("Testar under " + gatlingDurationOfTest + " minuter.")
    println("Antal användare: " + gatlingAntalAnvandare)
    println("BaseURL: " + gatlingBaseUrl)
  }

  val httpProtocol: HttpProtocolBuilder = http
    .baseUrl(gatlingBaseUrl)
    .acceptHeader("*/*")




  def gatlingBaseUrl: String = getProperty("testBaseURL", "http://127.0.0.1:8080")

  def getProperty(propertyName: String, defaultValue: String): String = {
    Option(System.getenv(propertyName))
      .orElse(Option(System.getProperty(propertyName)))
      .getOrElse(defaultValue)
  }

  def gatlingAntalAnvandare: Int = getProperty("antalAnvandare", "30").toInt

  def gatlingDurationOfTest: Int = getProperty("testDurationMinutes", "2").toInt


  def hamtaKommuner(): ChainBuilder = {
    exec(http("Hämta kommuner")
      .get("/grunddata/v1/kommuner")
      .check(status.is(200), jsonPath("$..kommunkod").count.is(290))
    )
  }




  setUp(
    scenario("Hämta kommuner från BFF")
      .forever() {
        exec(hamtaKommuner())
          // .pause(1)
          // .exec(hamtaForslagPaTrakt())
          .pause(1)
          .exec(sokFastighet())
      }.inject(atOnceUser(1))
  ).protocols(httpProtocol).maxDuration(gatlingDurationOfTest minute)
    .assertions(
      global.responseTime.max.lt(1000),
      global.successfulRequests.percent.gt(99)
    )
}