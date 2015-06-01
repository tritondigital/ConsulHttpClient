package com.tritondigital.consul.http.client

import com.ilunin.spray.gun.Server
import com.ning.http.client.{AsyncHttpClient, Response}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfterAll, Matchers, GivenWhenThen, FeatureSpec}
import spray.http.HttpResponse

class CacheFeature extends FeatureSpec with GivenWhenThen with Matchers with ScalaFutures with BeforeAndAfterAll{

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

    scenario("The consul client should use the cached resolution while waiting on Consul for changes") {
      Given("""a HTTP server that always returns 200 with "test" as the body""")
      val server = Server.simpleServer(interface = "localhost", port = 9999, content = "test")
      Server.executeWhileRunning(server) {

        And("a Consul Server that wait for changes on the second call")
        val consul = new Consul
        Server.executeWhileRunning(consul.server) {

          When("the HTTP client do three requests")
          val response1: Response = client.prepareGet("http://myService.service.consul").withConsul().execute().futureValue
          val response2: Response = client.prepareGet("http://myService.service.consul").withConsul().execute().futureValue
          val response3: Response = client.prepareGet("http://myService.service.consul").withConsul().execute().futureValue

          Then("the status code returned by the client should be 200")
          response1.getStatusCode should be(200)
          response2.getStatusCode should be(200)
          response3.getStatusCode should be(200)

          And("""the body should be "test"""")
          response1.getResponseBody should be("test")
          response2.getResponseBody should be("test")
          response3.getResponseBody should be("test")

          And("the consul server should be called only twice")
          consul.called should be (2)
        }
      }
    }

    class Consul {
      var called = 0

      val server = Server.syncServer(port = 8500) {
        case _ =>
          called += 1
          if (called == 1) {
            HttpResponse(200, consulJson)
          } else {
            Thread.sleep(2000)
            HttpResponse(200, consulJson)
          }
      }
    }
  }
}
