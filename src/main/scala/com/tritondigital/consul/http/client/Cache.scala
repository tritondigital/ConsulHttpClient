package com.tritondigital.consul.http.client

import spray.caching.LruCache

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

class Cache(expiration: Duration = Duration.Inf, capacity: Int = Int.MaxValue) {
  
  val lruCache = LruCache[Seq[Node]](
    timeToIdle = expiration,
    timeToLive = expiration + 1.millis, // timeToLive must be greater than timeToIdle
    maxCapacity = capacity,
    initialCapacity = if (capacity > 16) 16 else capacity // initialCapacity must be less than maxCapacity
  )

  def apply(service: String, host: String, port: Int)(operation: => Future[Seq[Node]]): Future[Seq[Node]] = lruCache(Key(service, host, port))(operation)

  private case class Key(service: String, host: String, port: Int)

}
