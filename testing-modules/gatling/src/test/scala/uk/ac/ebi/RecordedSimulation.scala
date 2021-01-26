package uk.ac.ebi

import scala.concurrent.duration._

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._
import com.typesafe.config._

class RecordedSimulation extends Simulation {

  val dbSNPIds = csv("dbsnp.csv") circular

  val hgvss = csv("hgvs.csv") circular

  val pdbeaccessions = csv("pdbe_accessions.csv") circular

  val accessions = csv("accessions.csv") circular

  val geneNames = csv("gene_names.csv") circular

  val dbsnpjsons = csv("dbsnpjson.csv") circular

  val hgvsjsons = csv("hgvsjson.csv") circular

  val conf = ConfigFactory.load()
  val url = conf.getString("add.pepvep.base.url")
  val concurrentConsumer = conf.getInt("add.pepvep.concurrent.user")

  val httpProtocol = http.baseUrl(url)
    .inferHtmlResources(BlackList(""".*\.css""", """.*\.js""", """.*\.ico"""), WhiteList())
    .acceptHeader("application/json,text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
    .contentTypeHeader("application/json")
    .acceptEncodingHeader("gzip, deflate")
    .acceptLanguageHeader("it-IT,it;q=0.8,en-US;q=0.5,en;q=0.3")
    .userAgentHeader("Mozilla/5.0 (Windows NT 10.0; WOW64; rv:46.0) Gecko/20100101 Firefox/46.0")

  val scn = scenario("RecordedSimulation")
    .feed(dbSNPIds)
    .feed(hgvss)
    .feed(accessions)
    .feed(geneNames)
    .feed(pdbeaccessions)
    .feed(dbsnpjsons)
    .feed(hgvsjsons)
    .forever {
      exec(http("gene_request_bulk")
        .get("/genomic/${geneName}"))
        .exec(http("protein_request_bulk")
          .get("/protein/${accession}"))
        .exec(http("pdbe_request_bulk")
          .get("/structure/${pdbeaccession}"))
        .exec(http("dbsnp_request_bulk")
          .get("/dbSNP/${dbsnp}?species=homo_sapiens"))
        .exec(http("hgvs_request_bulk")
          .get("/hgvs/${hgvs}?species=homo_sapiens"))
        .exec(http("region_request_bulk")
          .get("/region/14%3A89993420-89993421?allele=A%2FG&species=homo_sapiens"))
        .exec(http("dbsnp_post_request_bulk")
          .post("/dbSNP")
          .body(StringBody("${dbsnpjson}")).asJson)
        .exec(http("hgvs_post_request_bulk")
          .post("/hgvs")
          .body(StringBody("${hgvsjson}")).asJson)
    }

  setUp(scn.inject(atOnceUsers(concurrentConsumer))).protocols(httpProtocol).maxDuration(conf.getInt("add.run.maxDuration") minutes)
}
