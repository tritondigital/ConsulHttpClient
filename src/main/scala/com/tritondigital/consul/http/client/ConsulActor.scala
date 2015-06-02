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
    case GetNodes(service) => getNodes(service)
    case Done((service, response)) => done(service, response)
  }

  private def getNodes(service: String) {
    lastSender = Some(sender())
    if (operations.get(service).isEmpty) {
      val consulResponse: Future[ConsulResponse] = listNodes(service, index)
      operations += service -> consulResponse
      sendResponseToSelf(service, consulResponse)
    }
  }

  private def done(service: String, response: ConsulResponse) {
    index = Some(response.index)
    operations -= service
    lastSender.foreach { actor =>
      actor ! CacheResult(service -> response.nodes)
    }
  }

  private def sendResponseToSelf(service: String, consulResponse: Future[ConsulResponse]) {
    val messageToSend: Future[Done] = consulResponse.map { response =>
      Done(service -> response)
    }
    messageToSend.pipeTo(self)
  }
}

object ConsulActor {

  def props(listNodes: (String, Option[Int]) => Future[ConsulResponse]) = Props(new ConsulActor(listNodes))

}

case class GetNodes(service: String)

case class Done(result: (String, ConsulResponse))
