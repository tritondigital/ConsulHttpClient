package com.tritondigital.consul.http.client

class StatusCodeException(host: String, port: Int, statusCode: String) extends RuntimeException {

  override def getMessage = s"Unable to get a response from Consul with host: $host, port: $port, statusCode: $statusCode"

}
