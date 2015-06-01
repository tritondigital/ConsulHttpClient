package com.tritondigital.consul.http.client

import com.ning.http.client.uri.Uri
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
        val uri = new Uri(request.getUri.getScheme, request.getUri.getUserInfo, node.ip, node.port, request.getUri.getPath, request.getUri.getQuery)
        requestBuilder.setUrl(uri.toUrl)
        new FutureRequestBuilder(requestBuilder).execute()
      }
    } else {
      new FutureRequestBuilder(requestBuilder).execute()
    }
  }

}
