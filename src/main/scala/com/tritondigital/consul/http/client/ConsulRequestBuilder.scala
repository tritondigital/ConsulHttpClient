package com.tritondigital.consul.http.client

import com.ning.http.client.{AsyncHttpClient, Response}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ConsulRequestBuilder(requestBuilder: AsyncHttpClient#BoundRequestBuilder) {

  def withConsul(): ConsulRequestBuilder = this

  def execute(): Future[Response] = {
    val resolve: String => Future[Node] = ConsulClient.resolve
    val futureRequestBuilder = RequestBuilderUtils.replaceIpAndPortIfNecessary(requestBuilder, resolve)
    futureRequestBuilder.flatMap(new FutureRequestBuilder(_).execute())
  }

}
