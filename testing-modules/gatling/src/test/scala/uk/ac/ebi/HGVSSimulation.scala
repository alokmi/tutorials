package uk.ac.ebi

import scala.concurrent.duration._
import com.typesafe.config._

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._

class HGVSSimulation extends Simulation {

  val hgvss = csv("hgvs.csv") circular
  val conf = ConfigFactory.load()
  val url = conf.getString("add.pepvep.base.url")
  val concurrentConsumer = conf.getInt("add.pepvep.concurrent.user")

  val httpProtocol = http
    .baseUrl(url)
    .inferHtmlResources(BlackList(""".*\.css""", """.*\.js""", """.*\.ico"""), WhiteList())
    .acceptHeader("text/html,application/json,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
    .acceptEncodingHeader("gzip, deflate")
    .acceptLanguageHeader("it-IT,it;q=0.8,en-US;q=0.5,en;q=0.3")
    .userAgentHeader("Mozilla/5.0 (Windows NT 10.0; WOW64; rv:46.0) Gecko/20100101 Firefox/46.0")

  val scn = scenario("HGVSSimulation")
    .feed(hgvss)
    .exec(http("hgvs_request")
      .get("/hgvs/${hgvs}"))
    .pause(5)

  setUp(scn.inject(atOnceUsers(concurrentConsumer))).protocols(httpProtocol)
}
