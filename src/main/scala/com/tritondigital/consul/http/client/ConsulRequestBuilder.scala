package com.tritondigital.consul.http.client

import com.ning.http.client.{AsyncHttpClient, Response}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ConsulRequestBuilder(requestBuilder: AsyncHttpClient#BoundRequestBuilder) {

  def withConsul(): ConsulRequestBuilder = this

  def execute(): Future[Response] = {
    val futureRequestBuilder = RequestBuilderUtils.replaceIpAndPortIfNecessary(requestBuilder, ConsulClient.resolve)
    futureRequestBuilder.flatMap(new FutureRequestBuilder(_).execute())
  }

  private def selectNode(future: Future[Seq[Node]]): Future[Node] = future.map { nodes =>
    nodes.head
  }
}
