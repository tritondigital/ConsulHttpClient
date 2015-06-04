package com.tritondigital.consul.http.client

import com.ilunin.spray.gun.Server
import com.ning.http.client.{AsyncHttpClient, Response}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfterAll, FeatureSpec, GivenWhenThen, Matchers}

class LoadBalancingFeature extends FeatureSpec with GivenWhenThen with Matchers with ScalaFutures with BeforeAndAfterAll {

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
      | },
      | {
      |   "Node": {
      |     "Address": "127.0.0.1"
      |   },
      |   "Service": {
      |     "Port": 9998
      |   },
      | }
      |]
    """.stripMargin


  feature("Load Balancing") {

    scenario("The consul client should load balance between nodes") {
      Given( """a HTTP server that always returns 200 with "test" as the body on port 9999""")
      val server = Server.simpleServer(interface = "localhost", port = 9999, content = "test")
      Server.executeWhileRunning(server) {

        And( """a HTTP server that always returns 200 with "test2" as the body on port 9998""")
        val server2 = Server.simpleServer(interface = "localhost", port = 9998, content = "test2")
        Server.executeWhileRunning(server2) {

          And("a Consul Server that return two nodes for myService")
          val consul = Server.simpleServer(port = 8500, content = consulJson)
          Server.executeWhileRunning(consul) {

            When("the HTTP client do three requests")
            val response1: Response = client.prepareGet("http://myService.service.consul").withConsul().execute().futureValue
            val response2: Response = client.prepareGet("http://myService.service.consul").withConsul().execute().futureValue

            Then("the status code returned by the client should be 200")
            response1.getStatusCode should be(200)
            response2.getStatusCode should be(200)

            And( """the body should be "test"""")
            response1.getResponseBody should be("test")
            response2.getResponseBody should be("test2")
          }
        }
      }
    }

  }

  override def afterAll(): Unit = {
    ConsulClient.reset()
    client.close()
  }
}
