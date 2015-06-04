package com.tritondigital.consul.http.client

import akka.actor.{ActorRef, ActorSystem}
import akka.pattern.ask
import akka.testkit.{TestKit, TestProbe}
import akka.util.Timeout
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfterAll, Matchers, fixture}

import scala.concurrent.Await
import scala.concurrent.duration._

class CacheActorTest(actorSystem: ActorSystem) extends TestKit(actorSystem) with fixture.WordSpecLike with Matchers with ScalaFutures with BeforeAndAfterAll {

  def this() = this(ActorSystem())

  implicit val timeout: Timeout = 1.second

  case class FixtureParam(actor: ActorRef, probe: TestProbe)

  def withFixture(test: OneArgTest) = {
    val probe = TestProbe()
    val actor = actorSystem.actorOf(CacheActor.props(probe.ref))
    withFixture(test.toNoArgTest(FixtureParam(actor, probe)))
  }

  "A Cache Actor" when {
    "when it has a value in cache for this service" should {
      "return the value in cache" in { f =>
        f.actor ! CachedResult("myService" -> List(Node("127.0.0.2", 9998)))
        val future = f.actor ? GetNode("myService")
        future.futureValue should be(Node("127.0.0.2", 9998))
      }

      "fail the future if the value in cache is an empty list of nodes" in { f =>
        f.actor ! CachedResult("myService" -> List.empty)
        val future = f.actor ? GetNode("myService")
        val exception = the[NoNodeAvailableException] thrownBy {
          Await.result(future, 1.second)
        }
        exception.getMessage should be("No available node found for service: myService")
      }
    }

    "when it has no value in cache" should {
      "call the Consul actor" in { f =>
        val future = f.actor ? GetNode("myService")
        f.probe.expectMsg(1.second, GetNodes("myService"))
      }

      "wait for the response from the consul actor" in { f =>
        val future = f.actor ? GetNode("myService")
        future should not be 'completed
        f.actor ! CachedResult("myService" -> List(Node("127.0.0.2", 9998)))
        future.futureValue should be(Node("127.0.0.2", 9998))
      }
    }

    "when receiving a cached result" should {
      "return the result to all requesters waiting for this service" in { f =>
        val future1 = f.actor ? GetNode("myService")
        val future2 = f.actor ? GetNode("myService")
        val future3 = f.actor ? GetNode("otherService")
        future1 should not be 'completed
        future2 should not be 'completed
        future3 should not be 'completed
        f.actor ! CachedResult("myService" -> List(Node("127.0.0.2", 9998)))
        future1.futureValue should be(Node("127.0.0.2", 9998))
        future2.futureValue should be(Node("127.0.0.2", 9998))
        future3 should not be 'completed
      }

      "should call the Consul actor" in { f =>
        f.actor ! CachedResult("myService" -> List(Node("127.0.0.2", 9998)))
        f.probe.expectMsg(1.second, GetNodes("myService"))
      }
    }
  }

  override def afterAll() {
    TestKit.shutdownActorSystem(system)
  }
}
