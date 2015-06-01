package com.tritondigital.consul.http.client

import com.ilunin.spray.gun.Server
import com.ning.http.client.{AsyncHttpClient, Response}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfterAll, FeatureSpec, GivenWhenThen, Matchers}
import spray.http.ContentTypes

class ServiceDiscoveryFeature extends FeatureSpec with GivenWhenThen with Matchers with ScalaFutures with BeforeAndAfterAll {

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

  feature("Service Discovery") {

    scenario("The HTTP client should call directly the HTTP server if the host does not end in service.consul") {
      Given("""a HTTP server that always returns 200 with "test" as the body""")
      val server = Server.simpleServer(interface = "localhost", port = 9999, content = "test")
      Server.executeWhileRunning(server) {

        When("the HTTP client calls directly the server (No domain that ends in service.consul)")
        val response: Response = client.prepareGet("http://localhost:9999").withConsul().execute().futureValue

        Then("the status code returned by the client should be 200")
        response.getStatusCode should be(200)

        And("""body should be "test"""")
        response.getResponseBody should be("test")
      }
    }

    scenario("The HTTP client should call the ip and port returned by Consul if the host ends in service.consul") {
      Given("""a HTTP server listening on port 9999 and IP 127.0.0.1 that always returns 200 with "test" as the body""")
      val server = Server.simpleServer(interface = "127.0.0.1", port = 9999, content = "test")
      Server.executeWhileRunning(server) {

        And("a Consul server that returns localhost and 9999 for the IP and port of the myService service")
        val consul = Server.simpleServer(port = 8500, contentType = ContentTypes.`application/json`, content = consulJson)
        Server.executeWhileRunning(consul) {
          When("the HTTP client calls the service")
          val response: Response = client.prepareGet("http://myService.service.consul").withConsul().execute().futureValue

          Then("the status code returned by the client should be 200")
          response.getStatusCode should be(200)

          And("""body should be "test"""")
          response.getResponseBody should be("test")
        }
      }
    }

  }

  override def afterAll(): Unit = {
    client.close()
  }

}
