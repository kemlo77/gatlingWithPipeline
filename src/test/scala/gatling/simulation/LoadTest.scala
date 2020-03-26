package gatling.simulation

import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._
import io.gatling.http.protocol.HttpProtocolBuilder

import scala.concurrent.duration._

class LoadTest extends Simulation {

  before {
    println("Testar during " + durationOfTest + " minutes.")
    println("Number of users: " + numberOfUsers)
    println("BaseURL: " + baseUrl)
  }

  val httpProtocol: HttpProtocolBuilder = http
    .baseUrl(baseUrl)
    .acceptHeader("*/*")


  def baseUrl: String = getProperty("gatlingTestBaseURL", "https://www.google.com/")

  def numberOfUsers: Int = getProperty("gatlingNumberOfUsers", "1").toInt

  def durationOfTest: Int = getProperty("gatlingTestDurationMinutes", "2").toInt

  def getProperty(propertyName: String, defaultValue: String): String = {
    Option(System.getenv(propertyName))
      .orElse(Option(System.getProperty(propertyName)))
      .getOrElse(defaultValue)
  }


  val scn: ScenarioBuilder = scenario("BasicSimulation")
    .exec(http("request_1")
      .get("/"))
    .pause(5)


  setUp(
    scn.inject(atOnceUsers(numberOfUsers))
  ).protocols(httpProtocol).maxDuration(durationOfTest minute)
    .assertions(
      global.responseTime.max.lt(1000),
      global.successfulRequests.percent.gt(99)
    )

}