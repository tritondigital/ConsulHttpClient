package com.tritondigital.consul.http.client

import java.util.concurrent.atomic.AtomicInteger

class RoundRobinSelector[T](private var counter: AtomicInteger = new AtomicInteger(0)) {

  def select(seq: Seq[T]): Option[T] = {
    if (seq.isEmpty) {
      None
    } else {
      val index = Math.abs(counter.getAndIncrement % seq.size)
      Some(seq(index))
    }
  }

}
