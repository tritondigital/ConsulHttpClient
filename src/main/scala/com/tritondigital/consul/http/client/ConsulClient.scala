package com.tritondigital.consul.http.client

import com.ning.http.client.AsyncHttpClient

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


class ConsulClient(extractNodes: String => Seq[Node]) {

  val httpClient = new AsyncHttpClient()

  def resolve(host: String): Future[Seq[Node]] = {
    val service = host.substring(0, host.indexOf(".service.consul"))
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

case class Node(ip: String, port: Int)
