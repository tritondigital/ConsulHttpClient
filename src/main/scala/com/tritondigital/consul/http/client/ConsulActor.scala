package com.tritondigital.consul.http.client

import akka.actor.{Props, Actor}

import scala.concurrent.Future
import akka.pattern.pipe

class ConsulActor(listNodes: (String, Option[Int]) => Future[ConsulResponse]) extends Actor {

  implicit val executionContext = context.dispatcher

  var index: Option[Int] = None
  var cache: Map[String, ConsulResponse] = Map.empty

  override def receive: Receive = {
    case Service(service) =>
      val cachedResult: Option[Future[ConsulResponse]] = cache.get(service).map(Future.successful)
      val consulResponse: Future[ConsulResponse] = cachedResult.getOrElse(listNodes(service, index))
      consulResponse.map { response =>
        index = Some(response.index)
        response.nodes.head
      }.pipeTo(sender())
    case CachedResult((service, consulResponse)) => cache += (service -> consulResponse)
  }

}

object ConsulActor {

  def props(listNodes: (String, Option[Int]) => Future[ConsulResponse]) = Props(new ConsulActor(listNodes))

}

case class Service(service: String)

case class CachedResult(cachedResult: (String, ConsulResponse))
