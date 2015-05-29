package com.tritondigital.consul.http.client

import java.net.ConnectException

import com.ilunin.spray.gun.{Get, GetWithParameters, Server}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, WordSpec}
import spray.http.HttpResponse

import scala.concurrent.Await
import scala.concurrent.duration._

class ConsulClientTest extends WordSpec with Matchers with ScalaFutures {

  val consulClient = new ConsulClient((body) => List(Node("10.30.101.47", 32770), Node("10.30.101.48", 32771)))
  val json = "json"

  "Consul Client" when {

    "querying" should {
      "pass the passing parameter" in {
        var passingParameterPresent = false
        val consul = Server.syncServer(port = 8500) {
          case GetWithParameters("/v1/health/service/myService", query) =>
            passingParameterPresent = query.get("passing").isDefined
            HttpResponse(200)
        }
        Server.executeWhileRunning(consul) {
          whenReady(consulClient.resolve("myService.service.consul")) { nodes =>
            passingParameterPresent should be(true)
          }
        }
      }
    }

    "Consul return multiple nodes" should {
      "return a sequence of all the nodes with their IP and port" in {
        val consul = Server.syncServer(port = 8500) {
          case Get("/v1/health/service/myService") => HttpResponse(200, json)
        }
        Server.executeWhileRunning(consul) {
          consulClient.resolve("myService.service.consul").futureValue should contain allOf(Node("10.30.101.47", 32770), Node("10.30.101.48", 32771))
        }
      }
    }

    "Consul return a non 200 response" should {
      "fail the future with a StatusCodeException" in {
        val consul = Server.syncServer(port = 8500) {
          case Get("/v1/health/service/myService") => HttpResponse(404)
        }
        Server.executeWhileRunning(consul) {
          val exception = the[StatusCodeException] thrownBy {
            Await.result(consulClient.resolve("myService.service.consul"), 1.second)
          }

          exception.getMessage should be("Unable to get a response from Consul with host: localhost, port: 8500, statusCode: 404")
        }
      }
    }

    "no Consul server is up" should {
      "fail the future with a ConnectException" in {
        val exception = the[ConnectException] thrownBy {
          Await.result(consulClient.resolve("myService.service.consul"), 1.second)
        }

        exception.getMessage should be("Connection refused: localhost/127.0.0.1:8500")
      }
    }
  }

}
