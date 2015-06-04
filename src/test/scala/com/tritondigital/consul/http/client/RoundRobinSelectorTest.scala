package com.tritondigital.consul.http.client

import java.util.concurrent.atomic.AtomicInteger

import org.scalatest.{Matchers, WordSpec}

class RoundRobinSelectorTest extends WordSpec with Matchers {

  val list = List(1, 2, 3)

  "Node Selector" when {
    "starting with a counter of 0" should {
      "return the nodes in order" in {
        val selector = new RoundRobinSelector[Int]
        selector.select(list) should be (Some(1))
        selector.select(list) should be (Some(2))
        selector.select(list) should be (Some(3))
        selector.select(list) should be (Some(1))
      }
    }

    "starting with a counter of Integer.MAX_VALUE" should {
      "reverse the order after reset" in {
        val selector = new RoundRobinSelector[Int](new AtomicInteger(Integer.MAX_VALUE))
        selector.select(list) should be (Some(2))
        selector.select(list) should be (Some(3))
        selector.select(list) should be (Some(2))
        selector.select(list) should be (Some(1))
      }
    }

    "starting with a counter of -1" should {
      "reverse the order after 0" in {
        val selector = new RoundRobinSelector[Int](new AtomicInteger(-1))
        selector.select(list) should be (Some(2))
        selector.select(list) should be (Some(1))
        selector.select(list) should be (Some(2))
        selector.select(list) should be (Some(3))
      }
    }

    "given an empty list" should {
      "always return None" in {
        val selector = new RoundRobinSelector[Int]
        selector.select(List.empty) should be (None)
        selector.select(List.empty) should be (None)
      }
    }
  }

}
