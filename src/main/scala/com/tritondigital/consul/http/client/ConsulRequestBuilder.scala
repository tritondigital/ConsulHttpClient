package com.tritondigital.consul.http.client

import com.ning.http.client.{AsyncHttpClient, Response}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class ConsulRequestBuilder(requestBuilder: AsyncHttpClient#BoundRequestBuilder) {

  private val consulClient = new ConsulClient(Json.extractNodes)

  def withConsul(): ConsulRequestBuilder = this

  def execute(): Future[Response] = {
    val request = requestBuilder.build()
    val uri = request.getUri
    val host = uri.getHost
    if (host.endsWith(".service.consul")) {
      val nodes = consulClient.resolve(host)
      nodes.flatMap { nodes =>
        val node = nodes.head
        val client = new AsyncHttpClient()
        client.executeRequest(new ConsulRequest(request, node))
        new FutureRequestBuilder(requestBuilder).execute()
      }
    } else {
      new FutureRequestBuilder(requestBuilder).execute()
    }
  }

}
