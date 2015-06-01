package com.tritondigital.consul.http.client

import akka.actor.{ActorRef, ActorSystem}
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.Future
import scala.concurrent.duration._

object ConsulClient {

  implicit val timeout: Timeout = 1.second

  private val listNodes: (String, Option[Int]) => Future[ConsulResponse] = new ConsulHttpClient(Json.extractNodes).listNodes

  private val actorSystem = ActorSystem("consul")

  private val consulActor: ActorRef = actorSystem.actorOf(ConsulActor.props(listNodes), "consulActor")

  def resolve(service: String): Future[Node] = (consulActor ? Service(service)).mapTo[Node]

}

case class Node(ip: String, port: Int)

