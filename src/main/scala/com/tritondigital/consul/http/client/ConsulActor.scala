package com.tritondigital.consul.http.client

import akka.actor.{ActorRef, Actor, Props}
import akka.pattern.pipe

import scala.concurrent.Future

class ConsulActor(listNodes: (String, Option[Int]) => Future[ConsulResponse]) extends Actor {

  implicit val executionContext = context.dispatcher

  var index: Option[Int] = None
  var operations = Map.empty[String, Future[ConsulResponse]]
  var lastSender: Option[ActorRef] = None

  override def receive: Receive = {
    case GetNodes(service) =>
      lastSender = Some(sender())
      if (operations.get(service).isEmpty) {
        val consulResponse: Future[ConsulResponse] = listNodes(service, index)
        operations += service -> consulResponse
        consulResponse.map { response =>
          Done(service -> response)
        }.pipeTo(self)
      }
    case Done((service, response)) =>
      index = Some(response.index)
      operations -= service
      lastSender.foreach { actor =>
        actor ! CacheResult(service -> response.nodes)
      }
  }

}

object ConsulActor {

  def props(listNodes: (String, Option[Int]) => Future[ConsulResponse]) = Props(new ConsulActor(listNodes))

}

case class GetNodes(service: String)

case class Done(result: (String, ConsulResponse))
