package com.tritondigital.consul.http.client

import akka.actor.Status.Failure
import akka.actor.{Actor, ActorRef, Props}

class CacheActor(consulActor: ActorRef) extends Actor {

  var cache: Map[String, Seq[Node]] = Map.empty
  var awaitingSenders = Map.empty[String, Vector[ActorRef]]

  override def receive: Receive = {
    case GetNode(service) => getNode(service)
    case CacheResult((service, nodes)) => cacheResult(service, nodes)
  }

  private def getNode(service: String): Unit = {
    cache.get(service) match {
      case Some(nodes) => sender ! selectNode(service, nodes)
      case None =>
        updateAwaitingSenders(service)
        consulActor ! GetNodes(service)
    }
  }

  private def cacheResult(service: String, nodes: Seq[Node]) {
    cache += service -> nodes
    awaitingSenders.getOrElse(service, Vector.empty).foreach { actor =>
      actor ! selectNode(service, nodes)
    }
    consulActor ! GetNodes(service)
  }

  private def selectNode(service: String, nodes: Seq[Node]): Any = {
    if (nodes.isEmpty) {
      Failure(new NoNodeAvailableException(service))
    } else {
      nodes.head
    }
  }

  private def updateAwaitingSenders(service: String) {
    val senders: Vector[ActorRef] = awaitingSenders.getOrElse(service, Vector.empty)
    awaitingSenders = awaitingSenders.updated(service, senders :+ sender())
  }
}

object CacheActor {

  def props(consulActor: ActorRef) = Props(new CacheActor(consulActor))

}

case class GetNode(service: String)

case class CacheResult(cachedResult: (String, Seq[Node]))

