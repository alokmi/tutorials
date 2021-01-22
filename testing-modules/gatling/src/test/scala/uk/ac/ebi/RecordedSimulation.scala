package uk.ac.ebi

import scala.concurrent.duration._

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._

class RecordedSimulation extends Simulation {
  
    val dbSNPIds = csv("dbsnp.csv") circular
    
    val hgvss = csv("hgvs.csv") circular
    

    val httpProtocol = http
        .baseUrl("http://wp-np2-be:8099/pepvep")
//        .baseUrl("http://wp-np2-ca:8080/pepvep-service/pepvep")
        .inferHtmlResources(BlackList(""".*\.css""", """.*\.js""", """.*\.ico"""), WhiteList())
        .acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
        .acceptEncodingHeader("gzip, deflate")
        .acceptLanguageHeader("it-IT,it;q=0.8,en-US;q=0.5,en;q=0.3")
        .userAgentHeader("Mozilla/5.0 (Windows NT 10.0; WOW64; rv:46.0) Gecko/20100101 Firefox/46.0")



    val scn = scenario("RecordedSimulation")
        .feed(dbSNPIds)
        .feed(hgvss)
        .exec(http("gene_request")
            .get("/genomic/TDP1"))
        .pause(5)
        .exec(http("protein_request")
            .get("/protein/P21802"))
        .pause(5)
        .exec(http("pdbe_request")
            .get("/structure/6n0d"))
        .pause(4)
        .exec(http("dbsnp_request")
            .get("/dbSNP/${dbSNPId}?species=homo_sapiens"))
        .pause(2)
        .exec(http("hgvs_request")
            .get("/hgvs/${hgvs}?species=homo_sapiens"))
        .pause(2)
        .exec(http("region_request")
            .get("/region/14%3A89993420-89993421?allele=A%2FG&species=homo_sapiens"))
        .pause(1)
//        .exec(http("request_5")
//            .post("/dbSNP?species=homo_sapiens"))
//            .body(StringBody("[\"rs755551706\", \"rs121909224\"]")).asJson
//        .pause(2)
//        .exec(http("request_6")
//            .get("/computers?p=3"))

    setUp(scn.inject(atOnceUsers(1))).protocols(httpProtocol)
}
