package com.tritondigital.consul.http.client

import com.ning.http.client.AsyncHttpClient

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


class ConsulHttpClient(extractNodes: String => Seq[Node]) {

  val httpClient = new AsyncHttpClient()

  def listNodes(service: String, index: Option[Int]): Future[ConsulResponse] = {
    val requestBuilder = httpClient.prepareGet(s"http://localhost:8500/v1/health/service/$service?passing")
    index.foreach { i =>
      requestBuilder.addQueryParam("index", i.toString)
      requestBuilder.addQueryParam("wait", "10m")
    }
    val futureRequestBuilder = new FutureRequestBuilder(requestBuilder)
    futureRequestBuilder.execute().map { response =>
      if (response.getStatusCode != 200) {
        throw new StatusCodeException("localhost", 8500, response.getStatusCode.toString)
      }
      val body = response.getResponseBody
      val index: Int = Option(response.getHeader("X-Consul-Index")).map(_.toInt).getOrElse(0)
      ConsulResponse(extractNodes(body), index)
    }
  }

}

case class ConsulResponse(nodes: Seq[Node], index: Int)
