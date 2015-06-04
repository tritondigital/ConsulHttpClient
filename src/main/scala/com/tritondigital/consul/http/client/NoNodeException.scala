package com.tritondigital.consul.http.client

class NoNodeException(service: String) extends RuntimeException {

  override def getMessage = s"""No node found for service $service"""

}
