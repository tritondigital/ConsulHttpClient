package com.tritondigital.consul.http.client

import akka.actor.Status.Failure
import akka.actor.{Actor, ActorRef, Props}

class CacheActor(consulActor: ActorRef) extends Actor {

  var cache: Map[String, CacheEntry] = Map.empty
  var awaitingSenders = Map.empty[String, Vector[ActorRef]]

  override def receive: Receive = {
    case GetNode(service) => getNode(service)
    case CacheResult((service, nodes)) => cacheResult(service, nodes)
  }

  private def getNode(service: String): Unit = {
    cache.get(service) match {
      case Some(CacheEntry(nodes, index)) => sender ! selectNode(service, nodes, index)
      case None =>
        updateAwaitingSenders(service)
        consulActor ! GetNodes(service)
    }
  }

  private def cacheResult(service: String, nodes: Seq[Node]) {
    cache = cache.updated(service, CacheEntry(nodes))
    awaitingSenders.getOrElse(service, Vector.empty).foreach { actor =>
      actor ! selectNode(service, nodes)
    }
    consulActor ! GetNodes(service)
  }

  private def selectNode(service: String, nodes: Seq[Node], index: Int = 0): Any = {
    if (nodes.isEmpty) {
      Failure(new NoNodeAvailableException(service))
    } else {
      val node = nodes(index)
      cache = cache.updated(service, CacheEntry(nodes, (index + 1) % nodes.size))
      node
    }
  }

  private def updateAwaitingSenders(service: String) {
    val senders: Vector[ActorRef] = awaitingSenders.getOrElse(service, Vector.empty)
    awaitingSenders = awaitingSenders.updated(service, senders :+ sender())
  }

  case class CacheEntry(nodes: Seq[Node], index: Int = 0)
}

object CacheActor {

  def props(consulActor: ActorRef) = Props(new CacheActor(consulActor))

}

case class GetNode(service: String)

case class CacheResult(cachedResult: (String, Seq[Node]))

