package com.tritondigital.consul.http.client

import com.ning.http.client.AsyncHttpClient
import com.ning.http.client.uri.Uri

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object RequestBuilderUtils {

  def replaceIpAndPortIfNecessary(requestBuilder: AsyncHttpClient#BoundRequestBuilder, resolve: String => Future[Node]): Future[AsyncHttpClient#BoundRequestBuilder] = {
    val request = requestBuilder.build()
    val host = request.getUri.getHost
    if (host.endsWith(".service.consul")) {
      resolve(host).map { node =>
        val uri = new Uri(request.getUri.getScheme, request.getUri.getUserInfo, node.ip, node.port, request.getUri.getPath, request.getUri.getQuery)
        requestBuilder.resetQuery()
        requestBuilder.setUrl(uri.toUrl)
      }
    } else {
      Future.successful(requestBuilder)
    }
  }
  
}
