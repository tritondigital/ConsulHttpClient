package com.tritondigital.consul.http.client

class NoNodeAvailableException(service: String) extends RuntimeException {

  override def getMessage = s"No available node found for service: $service"

}
