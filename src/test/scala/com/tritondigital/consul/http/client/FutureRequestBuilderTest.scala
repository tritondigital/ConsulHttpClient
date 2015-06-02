package com.tritondigital.consul.http.client

import java.net.ConnectException

import com.ilunin.spray.gun.Server
import com.ning.http.client.{Response, AsyncHttpClient}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpec}

import scala.concurrent.Await
import scala.concurrent.duration._

class FutureRequestBuilderTest extends WordSpec with Matchers with ScalaFutures with BeforeAndAfterAll {

  val client = new AsyncHttpClient()
  val consulRequestBuilder = new FutureRequestBuilder(client.prepareGet("http://localhost:9999"))

  "FutureRequestBuilder" when {
    "receiving a response from the BoundRequestBuilder" should {
      "return a successful future with this response" in {
        val server = Server.simpleServer(port = 9999, content = "test")

        Server.executeWhileRunning(server) {
          val response: Response = consulRequestBuilder.execute().futureValue

          response.getStatusCode should be (200)
          response.getResponseBody should be ("test")
        }
      }
    }

    "trying to connect to a non existing server" should {
      "return a successful future with this response" in {
        a[ConnectException] should be thrownBy {
          Await.result(consulRequestBuilder.execute(), 1.second)
        }
      }
    }
  }

  override def afterAll(): Unit = {
    client.close()
  }

}
