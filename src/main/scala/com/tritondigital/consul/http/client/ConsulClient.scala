package com.tritondigital.consul.http.client

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ConsulClient(listNodes: String => Future[Seq[Node]]) {

  private var nodeSelector = new RoundRobinSelector[Node]

  def resolve(service: String): Future[Node] = listNodes(service).map { nodes =>
    nodeSelector.select(nodes).getOrElse {
      throw new NoNodeException(service)
    }
  }

  def reset(): Unit = nodeSelector = new RoundRobinSelector[Node]
}

case class Node(ip: String, port: Int)

object ConsulClient extends ConsulClient(ConsulHttpClient.listNodes)

