package com.tritondigital.consul.http.client

import akka.actor.{ActorRef, ActorSystem}
import akka.pattern.ask
import akka.testkit.TestProbe
import akka.util.Timeout
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpec}

import scala.concurrent.{Promise, Future}
import scala.concurrent.duration._

class ConsulActorTest extends WordSpec with Matchers with ScalaFutures with BeforeAndAfterAll {

  implicit val actorSystem = ActorSystem()

  implicit val timeout: Timeout = 1.second

  "The Consul Actor" when {

    "receiving a response" should {
      "send the CacheResult message with the response" in {
        val listNodes: (String, Option[Int]) => Future[ConsulResponse] = (service: String, index: Option[Int]) => {
          Future.successful(ConsulResponse(List(Node("127.0.0.1", 9999)), 2))
        }
        val probe = TestProbe()
        implicit val sender: ActorRef = probe.ref
        val actor = actorSystem.actorOf(ConsulActor.props(listNodes))
        actor ! GetNodes("myService")
        probe.expectMsg(1.second, CacheResult("myService" -> List(Node("127.0.0.1", 9999))))
      }
    }

    "receiving a response containing an index" should {
      "reuse the index in a subsequent call" in {
        var passedIndex: Option[Int] = None
        val listNodes: (String, Option[Int]) => Future[ConsulResponse] = (service: String, index: Option[Int]) => {
          passedIndex = index
          Future.successful(ConsulResponse(List(Node("127.0.0.1", 9999)), 2))
        }
        val actor = actorSystem.actorOf(ConsulActor.props(listNodes))
        whenReady(actor ? GetNodes("myService")) { _ =>
          passedIndex should not be 'defined
        }
        whenReady(actor ? GetNodes("myService")) { _ =>
          passedIndex should be(Some(2))
        }
      }
    }

    "already waiting for a response" should {
      "not call the listNodes function" in {
        var called = 0
        val promise = Promise[ConsulResponse]()
        val listNodes: (String, Option[Int]) => Future[ConsulResponse] = (service: String, index: Option[Int]) => {
          called += 1
          promise.future
        }
        val actor = actorSystem.actorOf(ConsulActor.props(listNodes))
        val probe = TestProbe()
        implicit val sender: ActorRef = probe.ref
        actor ! GetNodes("myService")
        actor ! GetNodes("myService")
        promise.success(ConsulResponse(List(Node("127.0.0.1", 9999)), 2))
        probe.expectMsg(1.second, CacheResult("myService" -> List(Node("127.0.0.1", 9999))))
        called should be (1)
      }
    }

  }

  override def afterAll(): Unit = {
    actorSystem.shutdown()
  }

}
