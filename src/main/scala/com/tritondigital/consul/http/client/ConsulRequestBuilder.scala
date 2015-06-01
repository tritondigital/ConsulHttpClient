package com.tritondigital.consul.http.client

import com.ning.http.client.{AsyncHttpClient, Response}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ConsulRequestBuilder(requestBuilder: AsyncHttpClient#BoundRequestBuilder) {

  private val consulClient = new ConsulClient(new ConsulHttpClient(Json.extractNodes).listNodes)

  def withConsul(): ConsulRequestBuilder = this

  def execute(): Future[Response] = {
    val resolve: String => Future[Node] = consulClient.resolve
    val futureRequestBuilder = RequestBuilderUtils.replaceIpAndPortIfNecessary(requestBuilder, resolve)
    futureRequestBuilder.flatMap(new FutureRequestBuilder(_).execute())
  }

  private def selectNode(future: Future[Seq[Node]]): Future[Node] = future.map { nodes =>
    nodes.head
  }


}
