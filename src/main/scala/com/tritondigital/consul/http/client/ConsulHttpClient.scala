package com.tritondigital.consul.http.client

import com.ning.http.client.AsyncHttpClient

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


class ConsulHttpClient(extractNodes: String => Seq[Node]) {

  val httpClient = new AsyncHttpClient()

  def listNodes(service: String, host: String = "localhost", port: Int = 8500): Future[Seq[Node]] = {
    val futureRequestBuilder = new FutureRequestBuilder(httpClient.prepareGet(s"http://$host:$port/v1/health/service/$service?passing"))
    futureRequestBuilder.execute().map { response =>
      if (response.getStatusCode != 200) {
        throw new StatusCodeException(host, port, response.getStatusCode.toString)
      }
      val body = response.getResponseBody
      extractNodes(body)
    }
  }

}

object ConsulHttpClient extends ConsulHttpClient(Json.extractNodes)
