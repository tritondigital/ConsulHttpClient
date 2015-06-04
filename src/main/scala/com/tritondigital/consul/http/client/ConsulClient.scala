package com.tritondigital.consul.http.client

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class ConsulClient(listNodes: String => Future[Seq[Node]]) {

  def resolve(service: String): Future[Node] = listNodes(service).map { nodes =>
    nodes.head
  }

}

case class Node(ip: String, port: Int)

