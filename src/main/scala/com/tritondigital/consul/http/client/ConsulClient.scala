package com.tritondigital.consul.http.client

import com.tritondigital.consul.http.client.ConsulClient.nodeSelector

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ConsulClient(listNodes: String => Future[Seq[Node]]) {

  def resolve(service: String): Future[Node] = listNodes(service).map { nodes =>
    nodeSelector.select(nodes).getOrElse {
      throw new NoNodeException(service)
    }
  }

}

case class Node(ip: String, port: Int)

object ConsulClient {

  private var nodeSelector = new RoundRobinSelector[Node]

  def reset(): Unit = nodeSelector = new RoundRobinSelector[Node]

}

