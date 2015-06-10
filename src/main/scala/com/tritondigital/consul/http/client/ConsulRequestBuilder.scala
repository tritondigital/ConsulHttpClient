package com.tritondigital.consul.http.client

import com.ning.http.client.{AsyncHttpClient, Response}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ConsulRequestBuilder(requestBuilder: AsyncHttpClient#BoundRequestBuilder, host: String = "localhost", port: Int = 8500, cache: Option[Cache] = None) {

  def withConsul(host: String = "localhost", port: Int = 8500): ConsulRequestBuilder = new ConsulRequestBuilder(requestBuilder, host, port)

  def withCache(cache: Cache): ConsulRequestBuilder = new ConsulRequestBuilder(requestBuilder, host, port, Some(cache))

  def execute(): Future[Response] = {
    val resolve: (String, String, Int) => Future[Node] = ConsulClient.resolve(_, _, _, cache)
    val futureRequestBuilder = RequestBuilderUtils.replaceIpAndPortIfNecessary(requestBuilder, resolve(_, host, port))
    futureRequestBuilder.flatMap(new FutureRequestBuilder(_).execute())
  }

}
