package com.tritondigital.consul.http.client

import akka.actor.ActorSystem
import akka.pattern.ask
import akka.util.Timeout
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpec}

import scala.concurrent.duration._
import scala.concurrent.{Future, Promise}

class ConsulActorTest extends WordSpec with Matchers with ScalaFutures with BeforeAndAfterAll {

  val actorSystem = ActorSystem()
  implicit val timeout: Timeout = 1.second

  "The Consul Actor" when {
    "receiving a response containing an index" should {
      "reuse the index in a subsequent call" in {
        var passedIndex: Option[Int] = None
        val listNodes: (String, Option[Int]) => Future[ConsulResponse] = (service: String, index: Option[Int]) => {
          passedIndex = index
          Future.successful(ConsulResponse(List(Node("127.0.0.1", 9999)), 2))
        }
        val actor = actorSystem.actorOf(ConsulActor.props(listNodes))
        whenReady(actor ? GetNode("myService")) { _ =>
          passedIndex should not be 'defined
        }
        whenReady(actor ? GetNode("myService")) { _ =>
          passedIndex should be (Some(2))
        }
      }
    }

    "when it has no value in cache" should {
      "wait after the result of the listNodes function" in {
        val promise = Promise[ConsulResponse]()
        val listNodes: (String, Option[Int]) => Future[ConsulResponse] = (service: String, index: Option[Int]) => {
          promise.future
        }
        val actor = actorSystem.actorOf(ConsulActor.props(listNodes))
        val future = actor ? GetNode("myService")
        future.isCompleted should be(false)
        promise.success(ConsulResponse(List(Node("127.0.0.1", 9999)), 2))
        future.futureValue should be(Node("127.0.0.1", 9999))
      }
    }
  }

  override def afterAll(): Unit = {
    actorSystem.shutdown()
  }

}
