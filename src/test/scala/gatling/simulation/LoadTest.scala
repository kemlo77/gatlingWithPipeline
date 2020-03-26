package gatling.simulation

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.http.protocol.HttpProtocolBuilder

import scala.concurrent.duration._

class LoadTest extends Simulation {

  before {
    println("Testar during " + gatlingDurationOfTest + " minutes.")
    println("Number of users: " + gatlingNumberOfUsers)
    println("BaseURL: " + gatlingBaseUrl)
  }

  val httpProtocol: HttpProtocolBuilder = http
    .baseUrl(gatlingBaseUrl)
    .acceptHeader("*/*")


  def gatlingBaseUrl: String = getProperty("testBaseURL", "https://www.google.com/")

  def gatlingNumberOfUsers: Int = getProperty("antalAnvandare", "30").toInt

  def gatlingDurationOfTest: Int = getProperty("testDurationMinutes", "2").toInt

  def getProperty(propertyName: String, defaultValue: String): String = {
    Option(System.getenv(propertyName))
      .orElse(Option(System.getProperty(propertyName)))
      .getOrElse(defaultValue)
  }


  val scn = scenario("BasicSimulation")
    .exec(http("request_1")
      .get("/"))
    .pause(5)


  setUp(
    scn.inject(atOnceUsers(1))
  ).protocols(httpProtocol).maxDuration(gatlingDurationOfTest minute)
    .assertions(
      global.responseTime.max.lt(1000),
      global.successfulRequests.percent.gt(99)
    )

}