package com.tritondigital.consul.http.client

import com.ning.http.client.AsyncHttpClient

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


class ConsulHttpClient(extractNodes: String => Seq[Node]) {

  val httpClient = new AsyncHttpClient()

  def listNodes(service: String): Future[Seq[Node]] = {
    val futureRequestBuilder = new FutureRequestBuilder(httpClient.prepareGet(s"http://localhost:8500/v1/health/service/$service?passing"))
    futureRequestBuilder.execute().map { response =>
      if (response.getStatusCode != 200) {
        throw new StatusCodeException("localhost", 8500, response.getStatusCode.toString)
      }
      val body = response.getResponseBody
      extractNodes(body)
    }
  }

}
