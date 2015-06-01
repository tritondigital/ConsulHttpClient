package com.tritondigital.consul.http.client

import com.ning.http.client.AsyncHttpClient
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, WordSpec}

import scala.concurrent.Future

class RequestBuilderUtilsTest extends WordSpec with Matchers with ScalaFutures {

  val client = new AsyncHttpClient
  val resolve: String => Future[Node] = {
    case "myService" => Future.successful(Node("127.0.0.1", 9999))
  }

  "RequestBuilderUtils" when {
    "the given BoundRequestBuilder has a host that does not end in .service.consul" should {
      "return the given BoundRequestBuilder in a Future" in {
        val requestBuilder = client.prepareGet("http://localhost:9999/path")
        val result = RequestBuilderUtils.replaceIpAndPortIfNecessary(requestBuilder, resolve)
        result.futureValue should be(requestBuilder)
      }
    }

    "the given BoundRequestBuilder has a host that ends in .service.consul" should {
      "return a request builder that build a request with the host and ip resolved by the given resolve function" in {
        val requestBuilder = client.prepareGet("http://myService.service.consul/path")
        val result = RequestBuilderUtils.replaceIpAndPortIfNecessary(requestBuilder, resolve)
        result.futureValue.build().getUrl should be("http://127.0.0.1:9999/path")
      }

      "return a request builder that does not duplicates query parameters" in {
        val requestBuilder = client.prepareGet("http://myService.service.consul/path?param1=value1").addQueryParam("param2", "value2")
        val result = RequestBuilderUtils.replaceIpAndPortIfNecessary(requestBuilder, resolve)
        result.futureValue.build().getUrl should be("http://127.0.0.1:9999/path?param1=value1&param2=value2")
      }
    }
  }

}
