package com.tritondigital.consul.http.client

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ConsulClient(listNodes: (String, String, Int) => Future[Seq[Node]]) {

  private var nodeSelector = new RoundRobinSelector[Node]

  def resolve(service: String, host: String, port: Int, cacheOption: Option[Cache]): Future[Node] = {
    val future = cacheOption match {
      case None => listNodes(service, host, port)
      case Some(cache) => cache(service, host, port) {
        listNodes(service, host, port)
      }
    }
    future.map { nodes =>
      nodeSelector.select(nodes).getOrElse {
        throw new NoNodeException(service)
      }
    }
  }

  def reset(): Unit = nodeSelector = new RoundRobinSelector[Node]
}

case class Node(ip: String, port: Int)

object ConsulClient extends ConsulClient(ConsulHttpClient.listNodes)

