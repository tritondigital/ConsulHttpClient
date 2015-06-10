package com.tritondigital.consul.http.client

import org.scalatest.{Matchers, WordSpec}
import org.scalatest.concurrent.ScalaFutures

import scala.concurrent.Future
import scala.concurrent.duration._

class CacheTest extends WordSpec with ScalaFutures with Matchers {

  "The cache" when {
    "not empty for the service myService, the host localhost and the port 8500" should {
      "return the last result and not call the given function again if called with the service myService, the host localhost and the port 8500" in {
        val cache = new Cache()
        var called = 0
        def cachedOp(service: String, host: String, port: Int): Future[Seq[Node]] = cache(service, host, port) {
          called += 1
          Future.successful(List(Node("127.0.0.1", 9000)))
        }
        val result1 = cachedOp("myService", "localhost", 8500)
        val result2 = cachedOp("myService", "localhost", 8500)
        result1.futureValue should be (List(Node("127.0.0.1", 9000)))
        result2.futureValue should be (List(Node("127.0.0.1", 9000)))
        called should be (1)
      }

      "call the given function again if called with the service myOtherService, the host localhost and the port 8500" in {
        val cache = new Cache()
        var called = 0
        def cachedOp(service: String, host: String, port: Int): Future[Seq[Node]] = cache(service, host, port) {
          called += 1
          Future.successful(List(Node("127.0.0.1", 9000)))
        }
        val result1 = cachedOp("myService", "localhost", 8500)
        val result2 = cachedOp("myOtherService", "localhost", 8500)
        result1.futureValue should be (List(Node("127.0.0.1", 9000)))
        result2.futureValue should be (List(Node("127.0.0.1", 9000)))
        called should be (2)
      }

      "call the given function again if called with the service myService, the host myHost.com and the port 8500" in {
        val cache = new Cache()
        var called = 0
        def cachedOp(service: String, host: String, port: Int): Future[Seq[Node]] = cache(service, host, port) {
          called += 1
          Future.successful(List(Node("127.0.0.1", 9000)))
        }
        val result1 = cachedOp("myService", "localhost", 8500)
        val result2 = cachedOp("myService", "myHost.com", 8500)
        result1.futureValue should be (List(Node("127.0.0.1", 9000)))
        result2.futureValue should be (List(Node("127.0.0.1", 9000)))
        called should be (2)
      }

      "call the given function again if called with the service myService, the host localhost and the port 8300" in {
        val cache = new Cache()
        var called = 0
        def cachedOp(service: String, host: String, port: Int): Future[Seq[Node]] = cache(service, host, port) {
          called += 1
          Future.successful(List(Node("127.0.0.1", 9000)))
        }
        val result1 = cachedOp("myService", "localhost", 8500)
        val result2 = cachedOp("myService", "localhost", 8300)
        result1.futureValue should be (List(Node("127.0.0.1", 9000)))
        result2.futureValue should be (List(Node("127.0.0.1", 9000)))
        called should be (2)
      }

      "call the given function again after the given expiration" in {
        val cache = new Cache(expiration = 500.millis)
        var called = 0
        def cachedOp(service: String, host: String, port: Int): Future[Seq[Node]] = cache(service, host, port) {
          called += 1
          Future.successful(List(Node("127.0.0.1", 9000)))
        }
        val result1 = cachedOp("myService", "localhost", 8500)
        Thread.sleep(501)
        val result2 = cachedOp("myService", "localhost", 8500)
        result1.futureValue should be (List(Node("127.0.0.1", 9000)))
        result2.futureValue should be (List(Node("127.0.0.1", 9000)))
        called should be (2)
      }

      "call the given function again after the capacity is reached" in {
        val cache = new Cache(capacity = 1)
        var called = 0
        def cachedOp(service: String, host: String, port: Int): Future[Seq[Node]] = cache(service, host, port) {
          called += 1
          Future.successful(List(Node("127.0.0.1", 9000)))
        }
        val result1 = cachedOp("myService", "localhost", 8500)
        val result2 = cachedOp("myService2", "localhost", 8500)
        val result3 = cachedOp("myService", "localhost", 8500)
        result1.futureValue should be (List(Node("127.0.0.1", 9000)))
        result2.futureValue should be (List(Node("127.0.0.1", 9000)))
        result3.futureValue should be (List(Node("127.0.0.1", 9000)))
        called should be (3)
      }
    }

  }

}
