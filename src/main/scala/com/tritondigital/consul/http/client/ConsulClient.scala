package com.tritondigital.consul.http.client

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ConsulClient(listNodes: (String, String, Int) => Future[Seq[Node]]) {

  private var nodeSelector = new RoundRobinSelector[Node]

  def resolve(service: String, host: String, port: Int): Future[Node] = listNodes(service, host, port).map { nodes =>
    nodeSelector.select(nodes).getOrElse {
      throw new NoNodeException(service)
    }
  }

  def reset(): Unit = nodeSelector = new RoundRobinSelector[Node]
}

case class Node(ip: String, port: Int)

object ConsulClient extends ConsulClient(ConsulHttpClient.listNodes)

