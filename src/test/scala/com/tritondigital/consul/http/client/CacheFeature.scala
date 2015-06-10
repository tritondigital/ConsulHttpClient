package com.tritondigital.consul.http.client

import com.ilunin.spray.gun.Server
import com.ning.http.client.{AsyncHttpClient, Response}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfterAll, FeatureSpec, GivenWhenThen, Matchers}
import spray.http.HttpResponse

class CacheFeature extends FeatureSpec with GivenWhenThen with Matchers with ScalaFutures with BeforeAndAfterAll {

  val client = new AsyncHttpClient

  val consulJson =
    """
      |[
      | {
      |   "Node": {
      |     "Address": "127.0.0.1"
      |   },
      |   "Service": {
      |     "Port": 9999
      |   },
      | }
      |]
    """.stripMargin


  feature("Cache") {

    scenario("The HTTP client should call consul only once if the cache is active") {
      Given("""a HTTP server listening on port 9999 and IP 127.0.0.1 that always returns 200 with "test" as the body""")
      val server = Server.simpleServer(interface = "127.0.0.1", port = 9999, content = "test")
      Server.executeWhileRunning(server) {

        And("a Consul server that returns localhost and 9999 for the IP and port of the myService service")
        val consul = new Consul
        Server.executeWhileRunning(consul.server) {
          When("the HTTP client calls the service twice with the same cache")
          val cache = new Cache()
          val response1: Response = client.prepareGet("http://myService.service.consul").withConsul().withCache(cache).execute().futureValue
          val response2: Response = client.prepareGet("http://myService.service.consul").withConsul().withCache(cache).execute().futureValue

          Then("the status code returned by the client should always be 200")
          response1.getStatusCode should be(200)
          response2.getStatusCode should be(200)

          And("""body should always be "test"""")
          response1.getResponseBody should be("test")
          response2.getResponseBody should be("test")

          And("the Consul server should be once")
          consul.called should be (1)
        }
      }
    }

  }

  class Consul {
    var called = 0

    val server = Server.syncServer(port = 8500) {
      case _ =>
        called += 1
        HttpResponse(200, consulJson)
    }
  }

}
