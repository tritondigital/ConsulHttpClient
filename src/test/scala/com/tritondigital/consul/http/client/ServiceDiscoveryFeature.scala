package com.tritondigital.consul.http.client

import com.ilunin.spray.gun.Server
import com.ning.http.client.{AsyncHttpClient, Response}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfterAll, FeatureSpec, GivenWhenThen, Matchers}

class ServiceDiscoveryFeature extends FeatureSpec with GivenWhenThen with Matchers with ScalaFutures with BeforeAndAfterAll {

  val client = new AsyncHttpClient

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
      Given("""a HTTP server that always returns 200 with "test" as the body""")
      val server = Server.simpleServer(interface = "localhost", port = 9999, content = "test")
      Server.executeWhileRunning(server) {

        When("the HTTP client calls the service")
        val response: Response = client.prepareGet("http://myService.service.consul").withConsul().execute().futureValue

        Then("the status code returned by the client should be 200")
        response.getStatusCode should be(200)

        And("""body should be "test"""")
        response.getResponseBody should be("test")
      }
    }

  }

  override def afterAll: Unit = {
    client.close()
  }

}
